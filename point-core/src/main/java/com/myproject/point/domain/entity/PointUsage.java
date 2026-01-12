package com.myproject.point.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_usages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String pointKey;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String orderNumber;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long cancellableAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pointUsage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointUsageDetail> usageDetails = new ArrayList<>();

    @Builder
    public PointUsage(String pointKey, Long userId, String orderNumber, Long amount,
                      Long cancellableAmount, LocalDateTime createdAt) {
        this.pointKey = pointKey;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.cancellableAmount = cancellableAmount;
        this.createdAt = createdAt;
    }

    public void cancel(Long cancelAmount) {
        if (this.cancellableAmount < cancelAmount) {
            throw new IllegalArgumentException("취소 가능한 포인트가 부족합니다.");
        }
        this.cancellableAmount -= cancelAmount;
    }

    //== 연관관계 편의 및 캡슐화 메소드 ==//
    public void addUsageDetail(PointEarning pointEarning, Long useAmount) {
        PointUsageDetail usageDetail = PointUsageDetail.builder()
            .pointUsage(this)
            .pointEarning(pointEarning)
            .amount(useAmount)
            .build();
        this.usageDetails.add(usageDetail);
    }
}
