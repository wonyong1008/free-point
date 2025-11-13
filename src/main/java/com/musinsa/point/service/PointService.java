package com.musinsa.point.service;

import com.musinsa.point.domain.entity.PointEarning;
import com.musinsa.point.domain.entity.PointHistory;
import com.musinsa.point.domain.entity.PointUsage;
import com.musinsa.point.domain.entity.PointUsageDetail;
import com.musinsa.point.domain.repository.PointEarningRepository;
import com.musinsa.point.domain.repository.PointHistoryRepository;
import com.musinsa.point.domain.repository.PointUsageDetailRepository;
import com.musinsa.point.domain.repository.PointUsageRepository;
import com.musinsa.point.dto.request.AccumulateCancelRequest;
import com.musinsa.point.dto.request.AccumulateRequest;
import com.musinsa.point.dto.request.UseCancelRequest;
import com.musinsa.point.dto.request.UseRequest;
import com.musinsa.point.dto.response.BalanceResponse;
import com.musinsa.point.dto.response.PointHistoryResponse;
import com.musinsa.point.dto.response.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointEarningRepository pointEarningRepository;
    private final PointUsageRepository pointUsageRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointConfigService pointConfigService;

    private static final int DEFAULT_EXPIRATION_DAYS = 365;
    private static final int MIN_EXPIRATION_DAYS = 1;
    private static final int MAX_EXPIRATION_DAYS = 1825; // 5년

    @Transactional
    public PointResponse accumulate(AccumulateRequest request) {
        // 1회 최대 적립 금액 검증
        Long maxAccumulateAmount = pointConfigService.getMaxAccumulateAmount();
        if (request.getAmount() > maxAccumulateAmount) {
            throw new IllegalArgumentException(
                    String.format("1회 최대 적립 가능 포인트는 %d원입니다.", maxAccumulateAmount));
        }

        // 개인별 최대 보유 포인트 검증
        Long currentBalance = pointEarningRepository.getTotalRemainingAmountByUserId(request.getUserId());
        Long maxUserBalance = pointConfigService.getMaxUserBalance();
        if (currentBalance + request.getAmount() > maxUserBalance) {
            throw new IllegalArgumentException(
                    String.format("개인별 최대 보유 포인트는 %d원입니다.", maxUserBalance));
        }

        // 만료일 설정
        int expirationDays = request.getExpirationDays() != null ? request.getExpirationDays() : DEFAULT_EXPIRATION_DAYS;
        if (expirationDays < MIN_EXPIRATION_DAYS || expirationDays >= MAX_EXPIRATION_DAYS) {
            throw new IllegalArgumentException(
                    String.format("만료일은 %d일 이상 %d일 미만이어야 합니다.", MIN_EXPIRATION_DAYS, MAX_EXPIRATION_DAYS));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusDays(expirationDays);

        String pointKey = generatePointKey();
        PointEarning pointEarning = PointEarning.builder()
                .pointKey(pointKey)
                .userId(request.getUserId())
                .amount(request.getAmount())
                .remainingAmount(request.getAmount())
                .expirationDate(expirationDate)
                .isManual(request.getIsManual() != null ? request.getIsManual() : false)
                .createdAt(now)
                .build();

        pointEarning = pointEarningRepository.save(pointEarning);

        // 이력 저장
        Long newBalance = currentBalance + request.getAmount();
        saveHistory(request.getUserId(), PointHistory.PointHistoryType.ACCUMULATE, pointKey,
                null, request.getAmount(), newBalance, now);

        return PointResponse.builder()
                .pointKey(pointEarning.getPointKey())
                .userId(pointEarning.getUserId())
                .amount(pointEarning.getAmount())
                .remainingAmount(pointEarning.getRemainingAmount())
                .expirationDate(pointEarning.getExpirationDate())
                .isManual(pointEarning.getIsManual())
                .createdAt(pointEarning.getCreatedAt())
                .build();
    }

    @Transactional
    public void accumulateCancel(AccumulateCancelRequest request) {
        PointEarning pointEarning = pointEarningRepository.findByPointKey(request.getPointKey())
                .orElseThrow(() -> new IllegalArgumentException("포인트를 찾을 수 없습니다."));

        // 일부 사용된 경우 취소 불가
        if (!pointEarning.getAmount().equals(pointEarning.getRemainingAmount())) {
            throw new IllegalArgumentException("일부 사용된 포인트는 적립 취소할 수 없습니다.");
        }

        Long userId = pointEarning.getUserId();
        Long currentBalance = pointEarningRepository.getTotalRemainingAmountByUserId(userId);
        Long cancelAmount = pointEarning.getAmount();

        pointEarningRepository.delete(pointEarning);

        // 이력 저장
        Long newBalance = currentBalance - cancelAmount;
        saveHistory(userId, PointHistory.PointHistoryType.ACCUMULATE_CANCEL, request.getPointKey(),
                null, cancelAmount, newBalance, LocalDateTime.now());
    }

    @Transactional
    public PointResponse use(UseRequest request) {
        List<PointEarning> availablePoints = pointEarningRepository.findAvailablePointsByUserId(request.getUserId());

        if (availablePoints.isEmpty()) {
            throw new IllegalArgumentException("사용 가능한 포인트가 없습니다.");
        }

        Long totalAvailable = availablePoints.stream()
                .mapToLong(PointEarning::getRemainingAmount)
                .sum();

        if (totalAvailable < request.getAmount()) {
            throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
        }

        String pointKey = generatePointKey();
        LocalDateTime now = LocalDateTime.now();

        // 포인트 사용 처리
        Long remainingUseAmount = request.getAmount();
        PointUsage pointUsage = PointUsage.builder()
                .pointKey(pointKey)
                .userId(request.getUserId())
                .orderNumber(request.getOrderNumber())
                .amount(request.getAmount())
                .cancellableAmount(request.getAmount())
                .createdAt(now)
                .build();
        pointUsage = pointUsageRepository.save(pointUsage);

        // 포인트 사용 상세 저장
        for (PointEarning pointEarning : availablePoints) {
            if (remainingUseAmount <= 0) {
                break;
            }

            Long useAmount = Math.min(pointEarning.getRemainingAmount(), remainingUseAmount);
            pointEarning.use(useAmount);
            pointEarningRepository.save(pointEarning);

            PointUsageDetail detail = PointUsageDetail.builder()
                    .pointUsageId(pointUsage.getId())
                    .pointEarningId(pointEarning.getId())
                    .amount(useAmount)
                    .build();
            pointUsageDetailRepository.save(detail);

            remainingUseAmount -= useAmount;
        }

        // 이력 저장
        Long currentBalance = pointEarningRepository.getTotalRemainingAmountByUserId(request.getUserId());
        saveHistory(request.getUserId(), PointHistory.PointHistoryType.USE, pointKey,
                request.getOrderNumber(), request.getAmount(), currentBalance, now);

        return PointResponse.builder()
                .pointKey(pointKey)
                .userId(request.getUserId())
                .amount(request.getAmount())
                .remainingAmount(0L)
                .expirationDate(null)
                .isManual(false)
                .createdAt(now)
                .build();
    }

    @Transactional
    public void useCancel(UseCancelRequest request) {
        PointUsage pointUsage = pointUsageRepository.findByPointKey(request.getPointKey())
                .orElseThrow(() -> new IllegalArgumentException("포인트 사용 내역을 찾을 수 없습니다."));

        Long cancelAmount = request.getAmount() != null ? request.getAmount() : pointUsage.getCancellableAmount();

        if (pointUsage.getCancellableAmount() < cancelAmount) {
            throw new IllegalArgumentException("취소 가능한 포인트가 부족합니다.");
        }

        List<PointUsageDetail> details = pointUsageDetailRepository.findByPointUsageId(pointUsage.getId());

        Long remainingCancelAmount = cancelAmount;
        LocalDateTime now = LocalDateTime.now();

        for (PointUsageDetail detail : details) {
            if (remainingCancelAmount <= 0) {
                break;
            }

            PointEarning pointEarning = pointEarningRepository.findById(detail.getPointEarningId())
                    .orElseThrow(() -> new IllegalArgumentException("포인트를 찾을 수 없습니다."));

            Long cancelFromPoint = Math.min(detail.getAmount(), remainingCancelAmount);

            if (pointEarning.isExpired()) {
                // 만료된 포인트는 신규 적립 처리
                String newPointKey = generatePointKey();
                PointEarning newPointEarning = PointEarning.builder()
                        .pointKey(newPointKey)
                        .userId(pointEarning.getUserId())
                        .amount(cancelFromPoint)
                        .remainingAmount(cancelFromPoint)
                        .expirationDate(now.plusDays(DEFAULT_EXPIRATION_DAYS))
                        .isManual(false)
                        .createdAt(now)
                        .build();
                pointEarningRepository.save(newPointEarning);

                // 신규 적립 이력 저장
                Long currentBalance = pointEarningRepository.getTotalRemainingAmountByUserId(pointEarning.getUserId());
                saveHistory(pointEarning.getUserId(), PointHistory.PointHistoryType.ACCUMULATE, newPointKey,
                        null, cancelFromPoint, currentBalance, now);
            } else {
                // 만료되지 않은 포인트는 원래 적립으로 복구
                pointEarning.cancelUsage(cancelFromPoint);
                pointEarningRepository.save(pointEarning);
            }

            remainingCancelAmount -= cancelFromPoint;
        }

        pointUsage.cancel(cancelAmount);
        pointUsageRepository.save(pointUsage);

        // 이력 저장
        Long currentBalance = pointEarningRepository.getTotalRemainingAmountByUserId(pointUsage.getUserId());
        saveHistory(pointUsage.getUserId(), PointHistory.PointHistoryType.USE_CANCEL, request.getPointKey(),
                pointUsage.getOrderNumber(), cancelAmount, currentBalance, now);
    }

    public BalanceResponse getBalance(Long userId) {
        Long totalBalance = pointEarningRepository.getTotalRemainingAmountByUserId(userId);
        return BalanceResponse.builder()
                .userId(userId)
                .totalBalance(totalBalance)
                .build();
    }

    public List<PointHistoryResponse> getHistory(Long userId) {
        return pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(history -> PointHistoryResponse.builder()
                        .id(history.getId())
                        .userId(history.getUserId())
                        .type(history.getType().name())
                        .pointKey(history.getPointKey())
                        .orderNumber(history.getOrderNumber())
                        .amount(history.getAmount())
                        .balance(history.getBalance())
                        .createdAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private String generatePointKey() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private void saveHistory(Long userId, PointHistory.PointHistoryType type, String pointKey,
                             String orderNumber, Long amount, Long balance, LocalDateTime createdAt) {
        PointHistory history = PointHistory.builder()
                .userId(userId)
                .type(type)
                .pointKey(pointKey)
                .orderNumber(orderNumber)
                .amount(amount)
                .balance(balance)
                .createdAt(createdAt)
                .build();
        pointHistoryRepository.save(history);
    }
}