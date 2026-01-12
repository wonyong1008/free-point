package com.myproject.point.domain.repository;

import com.myproject.point.domain.entity.PointUsage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PointUsageRepository extends JpaRepository<PointUsage, Long> {

    Optional<PointUsage> findByPointKey(String pointKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PointUsage> findWithLockByPointKey(String pointKey);
}
