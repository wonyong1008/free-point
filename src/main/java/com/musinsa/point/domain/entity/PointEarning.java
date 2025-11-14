package com.musinsa.point.domain.entity;

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
@Table(name = "point_earnings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointEarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String pointKey;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long remainingAmount;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private Boolean isManual;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pointEarning")
    private List<PointUsageDetail> usageDetails = new ArrayList<>();

    @Builder
    public PointEarning(String pointKey, Long userId, Long amount, Long remainingAmount,
                 LocalDateTime expirationDate, Boolean isManual, LocalDateTime createdAt) {
        this.pointKey = pointKey;
        this.userId = userId;
        this.amount = amount;
        this.remainingAmount = remainingAmount;
        this.expirationDate = expirationDate;
        this.isManual = isManual;
        this.createdAt = createdAt;
    }

    public void use(Long useAmount) {
        if (this.remainingAmount < useAmount) {
            throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
        }
        this.remainingAmount -= useAmount;
    }

    public void cancelUsage(Long cancelAmount) {
        this.remainingAmount += cancelAmount;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(this.expirationDate);
    }

    public void expire() {
        this.remainingAmount = 0L;
    }
}
