package com.musinsa.point.service;

import com.musinsa.point.domain.entity.PointEarning;
import com.musinsa.point.domain.entity.PointHistory;
import com.musinsa.point.domain.repository.PointEarningRepository;
import com.musinsa.point.domain.repository.PointHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PointExpirationSchedulerTest {

    @Autowired
    private PointExpirationScheduler pointExpirationScheduler;

    @Autowired
    private PointEarningRepository pointEarningRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private Clock clock;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAll();
        pointEarningRepository.deleteAll();
    }

    @Test
    @DisplayName("만료 포인트 배치가 remainingAmount 를 0으로 만든다")
    void expirePoints() {
        PointEarning expired = PointEarning.builder()
                .pointKey("EXPIRED")
                .userId(10L)
                .amount(1_000L)
                .remainingAmount(1_000L)
                .expirationDate(LocalDateTime.now(clock).minusDays(1))
                .isManual(false)
                .createdAt(LocalDateTime.now(clock).minusDays(10))
                .build();

        PointEarning active = PointEarning.builder()
                .pointKey("ACTIVE")
                .userId(10L)
                .amount(1_000L)
                .remainingAmount(1_000L)
                .expirationDate(LocalDateTime.now(clock).plusDays(10))
                .isManual(false)
                .createdAt(LocalDateTime.now(clock))
                .build();

        pointEarningRepository.save(expired);
        pointEarningRepository.save(active);

        pointExpirationScheduler.expirePoints();

        PointEarning expiredAfter = pointEarningRepository.findByPointKey("EXPIRED").orElseThrow();
        assertThat(expiredAfter.getRemainingAmount()).isZero();

        long expireHistoryCount = pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(10L).stream()
                .filter(history -> history.getType() == PointHistory.PointHistoryType.EXPIRE)
                .count();
        assertThat(expireHistoryCount).isEqualTo(1);
    }
}
