package com.myproject.point.domain.repository;

import com.myproject.point.domain.entity.PointEarning;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.myproject.point.domain.entity.QPointEarning.pointEarning;

@Repository
@RequiredArgsConstructor
public class PointEarningRepositoryCustomImpl implements PointEarningRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long getTotalRemainingAmountByUserId(Long userId, LocalDateTime asOf) {
        return queryFactory
                .select(pointEarning.remainingAmount.sum().coalesce(0L))
                .from(pointEarning)
                .where(
                        pointEarning.userId.eq(userId),
                        pointEarning.remainingAmount.gt(0),
                        pointEarning.expirationDate.goe(asOf)
                )
                .fetchOne();
    }

    @Override
    public List<PointEarning> findAvailablePointsByUserId(Long userId, LocalDateTime asOf) {
        return baseAvailableQuery(userId, asOf).fetch();
    }

    @Override
    public List<PointEarning> findAvailablePointsByUserIdForUpdate(Long userId, LocalDateTime asOf) {
        return baseAvailableQuery(userId, asOf)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }

    @Override
    public List<PointEarning> findExpiredPointEarnings(LocalDateTime asOf, int batchSize) {
        return queryFactory
                .selectFrom(pointEarning)
                .where(
                        pointEarning.expirationDate.lt(asOf),
                        pointEarning.remainingAmount.gt(0)
                )
                .orderBy(pointEarning.expirationDate.asc())
                .limit(batchSize)
                .fetch();
    }

    private JPAQuery<PointEarning> baseAvailableQuery(Long userId, LocalDateTime asOf) {
        return queryFactory
                .selectFrom(pointEarning)
                .where(
                        pointEarning.userId.eq(userId),
                        pointEarning.remainingAmount.gt(0),
                        pointEarning.expirationDate.goe(asOf)
                )
                .orderBy(
                        pointEarning.isManual.desc(),
                        pointEarning.expirationDate.asc()
                );
    }
}
