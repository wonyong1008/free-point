package com.musinsa.point.service;

import com.musinsa.point.domain.entity.PointEarning;
import com.musinsa.point.domain.entity.PointHistory;
import com.musinsa.point.domain.entity.PointUsage;
import com.musinsa.point.domain.entity.PointUsageDetail;
import com.musinsa.point.domain.repository.PointEarningRepository;
import com.musinsa.point.domain.repository.PointHistoryRepository;
import com.musinsa.point.domain.repository.PointUsageRepository;
import com.musinsa.point.dto.request.AccumulateCancelRequest;
import com.musinsa.point.dto.request.AccumulateRequest;
import com.musinsa.point.dto.request.UseCancelRequest;
import com.musinsa.point.dto.request.UseRequest;
import com.musinsa.point.dto.response.BalanceResponse;
import com.musinsa.point.dto.response.PointHistoryResponse;
import com.musinsa.point.dto.response.PointResponse;
import com.musinsa.point.exception.ErrorCode;
import com.musinsa.point.exception.PointException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointEarningRepository pointEarningRepository;
    private final PointUsageRepository pointUsageRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointConfigService pointConfigService;
    private final PointHistoryWriter pointHistoryWriter;
    private final MeterRegistry meterRegistry;
    private final Clock clock;

    private static final int DEFAULT_EXPIRATION_DAYS = 365;
    private static final int MIN_EXPIRATION_DAYS = 1;
    private static final int MAX_EXPIRATION_DAYS = 1825; // 5년

    @Transactional
    public PointResponse accumulate(AccumulateRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);

        Long maxAccumulateAmount = pointConfigService.getMaxAccumulateAmount();
        if (request.getAmount() > maxAccumulateAmount) {
            throw new PointException(ErrorCode.EXCEED_MAX_ACCUMULATE, maxAccumulateAmount);
        }

        Long currentBalance = pointEarningRepository.getTotalRemainingAmountByUserId(request.getUserId(), now);
        Long maxUserBalance = pointConfigService.getMaxUserBalance();
        if (currentBalance + request.getAmount() > maxUserBalance) {
            throw new PointException(ErrorCode.EXCEED_MAX_BALANCE, maxUserBalance);
        }

        int expirationDays = request.getExpirationDays() != null ? request.getExpirationDays() : DEFAULT_EXPIRATION_DAYS;
        if (expirationDays < MIN_EXPIRATION_DAYS || expirationDays >= MAX_EXPIRATION_DAYS) {
            throw new PointException(ErrorCode.INVALID_EXPIRATION_DAYS, MIN_EXPIRATION_DAYS, MAX_EXPIRATION_DAYS);
        }

        String pointKey = generatePointKey();
        PointEarning pointEarning = PointEarning.builder()
                .pointKey(pointKey)
                .userId(request.getUserId())
                .amount(request.getAmount())
                .remainingAmount(request.getAmount())
                .expirationDate(now.plusDays(expirationDays))
                .isManual(Boolean.TRUE.equals(request.getIsManual()))
                .createdAt(now)
                .build();

        pointEarningRepository.save(pointEarning);
        Long newBalance = currentBalance + request.getAmount();
        pointHistoryWriter.record(request.getUserId(), PointHistory.PointHistoryType.ACCUMULATE, pointKey,
                null, request.getAmount(), newBalance, now);

        log.info("Point accumulated. userId={} pointKey={} amount={}", request.getUserId(), pointKey, request.getAmount());
        meterRegistry.counter("point.accumulate.success").increment();

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
        LocalDateTime now = LocalDateTime.now(clock);
        PointEarning pointEarning = pointEarningRepository.findWithLockByPointKey(request.getPointKey())
                .orElseThrow(() -> new PointException(ErrorCode.POINT_NOT_FOUND));

        if (!pointEarning.getAmount().equals(pointEarning.getRemainingAmount())) {
            throw new PointException(ErrorCode.POINT_ALREADY_USED);
        }

        Long userId = pointEarning.getUserId();
        Long currentBalance = pointEarningRepository.getTotalRemainingAmountByUserId(userId, now);
        Long cancelAmount = pointEarning.getAmount();

        pointEarningRepository.delete(pointEarning);

        Long newBalance = Math.max(currentBalance - cancelAmount, 0);
        pointHistoryWriter.record(userId, PointHistory.PointHistoryType.ACCUMULATE_CANCEL, request.getPointKey(),
                null, cancelAmount, newBalance, now);
        log.info("Point accumulate cancelled. userId={} pointKey={}", userId, request.getPointKey());
    }

    @Transactional
    public PointResponse use(UseRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<PointEarning> availablePoints =
                pointEarningRepository.findAvailablePointsByUserIdForUpdate(request.getUserId(), now);

        if (availablePoints.isEmpty()) {
            throw new PointException(ErrorCode.NO_AVAILABLE_POINTS);
        }

        long totalAvailable = availablePoints.stream()
                .mapToLong(PointEarning::getRemainingAmount)
                .sum();

        if (totalAvailable < request.getAmount()) {
            throw new PointException(ErrorCode.INSUFFICIENT_POINT);
        }

        String pointKey = generatePointKey();
        PointUsage pointUsage = PointUsage.builder()
                .pointKey(pointKey)
                .userId(request.getUserId())
                .orderNumber(request.getOrderNumber())
                .amount(request.getAmount())
                .cancellableAmount(request.getAmount())
                .createdAt(now)
                .build();

        long remainingUseAmount = request.getAmount();
        for (PointEarning pointEarning : availablePoints) {
            if (remainingUseAmount <= 0) {
                break;
            }
            long useAmount = Math.min(pointEarning.getRemainingAmount(), remainingUseAmount);
            pointEarning.use(useAmount);
            pointUsage.addUsageDetail(pointEarning, useAmount);
            remainingUseAmount -= useAmount;
        }

        pointUsageRepository.save(pointUsage);

        Long currentBalance = pointEarningRepository.getTotalRemainingAmountByUserId(request.getUserId(), now);
        pointHistoryWriter.record(request.getUserId(), PointHistory.PointHistoryType.USE, pointKey,
                request.getOrderNumber(), request.getAmount(), currentBalance, now);
        log.info("Point used. userId={} pointKey={} orderNumber={} amount={}",
                request.getUserId(), pointKey, request.getOrderNumber(), request.getAmount());
        meterRegistry.counter("point.use.success").increment();

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
        LocalDateTime now = LocalDateTime.now(clock);
        PointUsage pointUsage = pointUsageRepository.findWithLockByPointKey(request.getPointKey())
                .orElseThrow(() -> new PointException(ErrorCode.POINT_USAGE_NOT_FOUND));

        Long cancelAmount = request.getAmount() != null ? request.getAmount() : pointUsage.getCancellableAmount();
        if (cancelAmount == null || cancelAmount < 1) {
            throw new PointException(ErrorCode.INVALID_CANCEL_AMOUNT);
        }

        if (pointUsage.getCancellableAmount() < cancelAmount) {
            throw new PointException(ErrorCode.CANCEL_AMOUNT_EXCEEDS);
        }

        List<PointUsageDetail> details = pointUsage.getUsageDetails().stream()
                .sorted(Comparator.comparing(PointUsageDetail::getId).reversed())
                .collect(Collectors.toList());

        long remainingCancelAmount = cancelAmount;
        Long balanceBefore = pointEarningRepository.getTotalRemainingAmountByUserId(pointUsage.getUserId(), now);
        long runningBalance = balanceBefore;

        for (PointUsageDetail detail : details) {
            if (remainingCancelAmount <= 0) {
                break;
            }

            PointEarning pointEarning = detail.getPointEarning();
            long cancelFromPoint = Math.min(detail.getAmount(), remainingCancelAmount);

            if (pointEarning.isExpired(now)) {
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

                runningBalance += cancelFromPoint;
                pointHistoryWriter.record(pointEarning.getUserId(), PointHistory.PointHistoryType.ACCUMULATE, newPointKey,
                        "사용취소로 인한 재적립", cancelFromPoint, runningBalance, now);
            } else {
                pointEarning.cancelUsage(cancelFromPoint);
                runningBalance += cancelFromPoint;
            }

            remainingCancelAmount -= cancelFromPoint;
        }

        pointUsage.cancel(cancelAmount);

        pointHistoryWriter.record(pointUsage.getUserId(), PointHistory.PointHistoryType.USE_CANCEL, request.getPointKey(),
                pointUsage.getOrderNumber(), cancelAmount, runningBalance, now);
        log.info("Point usage cancelled. userId={} pointKey={} cancelAmount={}",
                pointUsage.getUserId(), request.getPointKey(), cancelAmount);
        meterRegistry.counter("point.use-cancel.success").increment();
    }

    public BalanceResponse getBalance(Long userId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Long totalBalance = pointEarningRepository.getTotalRemainingAmountByUserId(userId, now);
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
}
