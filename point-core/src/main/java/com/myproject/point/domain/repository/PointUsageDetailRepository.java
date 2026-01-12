package com.myproject.point.domain.repository;

import com.myproject.point.domain.entity.PointUsageDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointUsageDetailRepository extends JpaRepository<PointUsageDetail, Long> {

    List<PointUsageDetail> findByPointUsageId(Long pointUsageId);
}

