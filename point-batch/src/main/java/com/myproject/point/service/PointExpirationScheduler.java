package com.myproject.point.service;

import com.myproject.point.config.PointProperties;
import com.myproject.point.domain.entity.PointEarning;
import com.myproject.point.domain.entity.PointHistory;
import com.myproject.point.domain.repository.PointEarningRepository;
import com.myproject.point.service.PointHistoryWriter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointExpirationScheduler {

    private final PointEarningRepository pointEarningRepository;
    private final PointHistoryWriter pointHistoryWriter;
    private final PointProperties pointProperties;
    private final MeterRegistry meterRegistry;
    private final Clock clock;

    @Transactional
    @Scheduled(cron = "${point.expiration.cron:0 0 * * * *}")
    public void expirePoints() {
        LocalDateTime now = LocalDateTime.now(clock);
        int batchSize = pointProperties.getExpiration().getBatchSize();
        List<PointEarning> expiredPoints = pointEarningRepository.findExpiredPointEarnings(now, batchSize);

        if (expiredPoints.isEmpty()) {
            return;
        }

        log.info("Expiring {} point rows at {}", expiredPoints.size(), now);
        Map<Long, Long> balanceCache = new HashMap<>();

        expiredPoints.forEach(pointEarning -> {
            Long remainingAmount = pointEarning.getRemainingAmount();
            if (remainingAmount <= 0) {
                return;
            }
            Long userId = pointEarning.getUserId();
            Long balance = balanceCache.computeIfAbsent(userId,
                    id -> pointEarningRepository.getTotalRemainingAmountByUserId(id, now));

            pointEarning.expire();
            pointHistoryWriter.record(userId,
                    PointHistory.PointHistoryType.EXPIRE,
                    pointEarning.getPointKey(),
                    null,
                    remainingAmount,
                    Math.max(balance, 0L),
                    now);
        });

        meterRegistry.counter("point.expire.count").increment((double) expiredPoints.size());
    }
}
