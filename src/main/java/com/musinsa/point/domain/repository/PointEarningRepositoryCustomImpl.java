package com.musinsa.point.domain.repository;

import com.musinsa.point.domain.entity.PointEarning;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.musinsa.point.domain.entity.QPointEarning.pointEarning;

@Repository
@RequiredArgsConstructor
public class PointEarningRepositoryCustomImpl implements PointEarningRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long getTotalRemainingAmountByUserId(Long userId) {
        return queryFactory
                .select(pointEarning.remainingAmount.sum().coalesce(0L))
                .from(pointEarning)
                .where(pointEarning.userId.eq(userId))
                .fetchOne();
    }

    @Override
    public List<PointEarning> findAvailablePointsByUserId(Long userId) {
        return queryFactory
                .selectFrom(pointEarning)
                .where(
                        pointEarning.userId.eq(userId),
                        pointEarning.remainingAmount.gt(0)
                )
                .orderBy(
                        pointEarning.isManual.desc(),
                        pointEarning.expirationDate.asc()
                )
                .fetch();
    }
}
