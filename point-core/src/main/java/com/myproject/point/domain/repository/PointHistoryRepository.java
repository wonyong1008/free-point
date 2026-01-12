package com.myproject.point.domain.repository;

import com.myproject.point.domain.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}

