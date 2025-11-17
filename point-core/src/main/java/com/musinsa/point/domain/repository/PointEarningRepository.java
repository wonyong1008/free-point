package com.musinsa.point.domain.repository;

import com.musinsa.point.domain.entity.PointEarning;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PointEarningRepository extends JpaRepository<PointEarning, Long>, PointEarningRepositoryCustom {

    Optional<PointEarning> findByPointKey(String pointKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PointEarning> findWithLockByPointKey(String pointKey);
}
