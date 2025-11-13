package com.musinsa.point.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_usage_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsageDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pointUsageId;

    @Column(nullable = false)
    private Long pointEarningId;

    @Column(nullable = false)
    private Long amount;

    @Builder
    public PointUsageDetail(Long pointUsageId, Long pointEarningId, Long amount) {
        this.pointUsageId = pointUsageId;
        this.pointEarningId = pointEarningId;
        this.amount = amount;
    }
}