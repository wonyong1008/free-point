package com.musinsa.point.domain.repository;

import com.musinsa.point.domain.entity.PointEarning;

import java.util.List;

public interface PointEarningRepositoryCustom {

    Long getTotalRemainingAmountByUserId(Long userId);

    List<PointEarning> findAvailablePointsByUserId(Long userId);
}
