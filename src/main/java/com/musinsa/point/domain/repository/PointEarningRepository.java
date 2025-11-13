package com.musinsa.point.domain.repository;

import com.musinsa.point.domain.entity.PointEarning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointEarningRepository extends JpaRepository<PointEarning, Long>, PointEarningRepositoryCustom {

    Optional<PointEarning> findByPointKey(String pointKey);

}
