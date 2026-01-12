package com.myproject.point.domain.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_usage_id", nullable = false)
    private PointUsage pointUsage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_earning_id", nullable = false)
    private PointEarning pointEarning;

    @Column(nullable = false)
    private Long amount;

    @Builder
    public PointUsageDetail(PointUsage pointUsage, PointEarning pointEarning, Long amount) {
        this.pointUsage = pointUsage;
        this.pointEarning = pointEarning;
        this.amount = amount;
    }
}
