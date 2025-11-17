package com.musinsa.point.service;

import com.musinsa.point.domain.entity.PointConfig;
import com.musinsa.point.domain.entity.PointEarning;
import com.musinsa.point.domain.entity.PointUsage;
import com.musinsa.point.domain.repository.PointConfigRepository;
import com.musinsa.point.domain.repository.PointEarningRepository;
import com.musinsa.point.domain.repository.PointUsageRepository;
import com.musinsa.point.dto.request.AccumulateRequest;
import com.musinsa.point.dto.request.UseCancelRequest;
import com.musinsa.point.dto.request.UseRequest;
import com.musinsa.point.dto.response.PointResponse;
import com.musinsa.point.exception.ErrorCode;
import com.musinsa.point.exception.PointException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest(classes = com.musinsa.point.PointCoreTestApplication.class)
@Transactional
class PointServiceTest {

    private static final Long USER_ID = 1L;

    @Autowired
    private PointService pointService;

    @Autowired
    private PointEarningRepository pointEarningRepository;

    @Autowired
    private PointUsageRepository pointUsageRepository;

    @Autowired
    private PointConfigRepository pointConfigRepository;

    @Autowired
    private Clock clock;

    @BeforeEach
    void setUp() {
        pointUsageRepository.deleteAll();
        pointEarningRepository.deleteAll();
        pointConfigRepository.deleteAll();
        pointConfigRepository.save(new PointConfig("MAX_ACCUMULATE_AMOUNT", 100000L));
        pointConfigRepository.save(new PointConfig("MAX_USER_BALANCE", 2000000L));
    }

    @Test
    @DisplayName("포인트 적립 성공")
    void accumulate_success() {
        AccumulateRequest request = AccumulateRequest.builder()
                .userId(USER_ID)
                .amount(5_000L)
                .build();

        PointResponse response = pointService.accumulate(request);

        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getAmount()).isEqualTo(5_000L);

        PointEarning saved = pointEarningRepository.findByPointKey(response.getPointKey()).orElseThrow();
        assertThat(saved.getRemainingAmount()).isEqualTo(5_000L);
    }

    @Test
    @DisplayName("포인트 적립 실패 - 1회 최대 적립 금액 초과")
    void accumulate_fail_exceedsMaxPerTransaction() {
        AccumulateRequest request = AccumulateRequest.builder()
                .userId(USER_ID)
                .amount(100_001L)
                .build();

        PointException exception = catchThrowableOfType(() -> pointService.accumulate(request), PointException.class);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_MAX_ACCUMULATE);
    }

    @Test
    @DisplayName("포인트 적립 실패 - 개인 최대 보유 금액 초과")
    void accumulate_fail_exceedsMaxTotal() {
        PointEarning initial = PointEarning.builder()
                .userId(USER_ID)
                .pointKey("INITIAL")
                .amount(1_950_000L)
                .remainingAmount(1_950_000L)
                .expirationDate(LocalDateTime.now(clock).plusDays(10))
                .isManual(false)
                .createdAt(LocalDateTime.now(clock))
                .build();
        pointEarningRepository.save(initial);

        AccumulateRequest request = AccumulateRequest.builder()
                .userId(USER_ID)
                .amount(100_000L)
                .build();

        PointException exception = catchThrowableOfType(() -> pointService.accumulate(request), PointException.class);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXCEED_MAX_BALANCE);
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void use_success() {
        pointService.accumulate(AccumulateRequest.builder()
                .userId(USER_ID)
                .amount(3_000L)
                .build());
        pointService.accumulate(AccumulateRequest.builder()
                .userId(USER_ID)
                .amount(5_000L)
                .isManual(true)
                .build());

        pointService.use(UseRequest.builder()
                .userId(USER_ID)
                .orderNumber("ORD-1")
                .amount(4_000L)
                .build());

        PointUsage usage = pointUsageRepository.findAll().get(0);
        assertThat(usage.getAmount()).isEqualTo(4_000L);
        assertThat(usage.getCancellableAmount()).isEqualTo(4_000L);
    }

    @Test
    @DisplayName("포인트 사용 취소 시 만료 포인트 재적립")
    void useCancel_withExpiredPoint_reEarned() {
        PointResponse response = pointService.accumulate(AccumulateRequest.builder()
                .userId(USER_ID)
                .amount(5_000L)
                .build());

        pointService.use(UseRequest.builder()
                .userId(USER_ID)
                .orderNumber("ORD-2")
                .amount(2_000L)
                .build());

        PointEarning earning = pointEarningRepository.findByPointKey(response.getPointKey()).orElseThrow();
        ReflectionTestUtils.setField(earning, "expirationDate", LocalDateTime.now(clock).minusDays(1));

        PointUsage usage = pointUsageRepository.findAll().get(0);
        UseCancelRequest cancelRequest = UseCancelRequest.builder()
                .pointKey(usage.getPointKey())
                .amount(1_000L)
                .build();

        pointService.useCancel(cancelRequest);

        LocalDateTime now = LocalDateTime.now(clock);
        long balance = pointEarningRepository.getTotalRemainingAmountByUserId(USER_ID, now);
        assertThat(balance).isGreaterThan(0L);
    }
}
