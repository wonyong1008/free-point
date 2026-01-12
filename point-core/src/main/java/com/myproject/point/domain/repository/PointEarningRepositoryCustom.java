package com.myproject.point.domain.repository;

import com.myproject.point.domain.entity.PointEarning;

import java.time.LocalDateTime;
import java.util.List;

public interface PointEarningRepositoryCustom {

    Long getTotalRemainingAmountByUserId(Long userId, LocalDateTime asOf);

    List<PointEarning> findAvailablePointsByUserId(Long userId, LocalDateTime asOf);

    List<PointEarning> findAvailablePointsByUserIdForUpdate(Long userId, LocalDateTime asOf);

    List<PointEarning> findExpiredPointEarnings(LocalDateTime asOf, int batchSize);
}
