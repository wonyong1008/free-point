package com.musinsa.point.service;

import com.musinsa.point.domain.entity.PointConfig;
import com.musinsa.point.domain.entity.PointEarning;
import com.musinsa.point.domain.repository.PointConfigRepository;
import com.musinsa.point.domain.repository.PointEarningRepository;
import com.musinsa.point.dto.request.AccumulateRequest;
import com.musinsa.point.dto.response.PointResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PointServiceTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointEarningRepository pointEarningRepository;

    @Autowired
    private PointConfigRepository pointConfigRepository;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        pointEarningRepository.deleteAll();
        pointConfigRepository.deleteAll();
        pointConfigRepository.save(new PointConfig("MAX_ACCUMULATE_AMOUNT", 100000L));
        pointConfigRepository.save(new PointConfig("MAX_USER_BALANCE", 2000000L));
    }

    @Test
    @DisplayName("포인트 적립 성공")
    void accumulate_success() {
        // given
        long amountToAccumulate = 5000L;
        AccumulateRequest request = AccumulateRequest.builder()
            .userId(USER_ID)
            .amount(amountToAccumulate)
            .build();

        // when
        PointResponse response = pointService.accumulate(request);

        // then
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getAmount()).isEqualTo(amountToAccumulate);
        assertThat(response.getRemainingAmount()).isEqualTo(amountToAccumulate);

        PointEarning savedEarning = pointEarningRepository.findByPointKey(response.getPointKey()).get();
        assertThat(savedEarning.getUserId()).isEqualTo(USER_ID);
        assertThat(savedEarning.getAmount()).isEqualTo(amountToAccumulate);

        long totalBalance = pointEarningRepository.getTotalRemainingAmountByUserId(USER_ID);
        assertThat(totalBalance).isEqualTo(amountToAccumulate);
    }

    @Test
    @DisplayName("포인트 적립 실패 - 1회 최대 적립 금액 초과")
    void accumulate_fail_exceedsMaxPerTransaction() {
        // given
        long amountToAccumulate = 100001L;
        AccumulateRequest request = AccumulateRequest.builder()
            .userId(USER_ID)
            .amount(amountToAccumulate)
            .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.accumulate(request);
        });
        assertThat(exception.getMessage()).contains("1회 최대 적립 가능 포인트는 100000원입니다.");
    }
    
    @Test
    @DisplayName("포인트 적립 실패 - 개인 최대 보유 금액 초과")
    void accumulate_fail_exceedsMaxTotal() {
        // given
        // 미리 1,950,000 포인트를 적립
        PointEarning initialEarning = PointEarning.builder()
            .userId(USER_ID)
            .pointKey("INITIAL_KEY")
            .amount(1950000L)
            .remainingAmount(1950000L)
            .expirationDate(LocalDateTime.now().plusDays(10))
            .isManual(false)
            .createdAt(LocalDateTime.now())
            .build();
        pointEarningRepository.save(initialEarning);

        long amountToAccumulate = 100000L;
        AccumulateRequest request = AccumulateRequest.builder()
            .userId(USER_ID)
            .amount(amountToAccumulate)
            .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.accumulate(request);
        });
        assertThat(exception.getMessage()).contains("개인별 최대 보유 포인트는 2000000원입니다.");
    }
}