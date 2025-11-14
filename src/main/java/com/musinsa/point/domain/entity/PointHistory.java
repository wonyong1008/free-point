package com.musinsa.point.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PointHistoryType type;

    @Column(nullable = false, length = 50)
    private String pointKey;

    @Column
    private String orderNumber;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balance;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public PointHistory(Long userId, PointHistoryType type, String pointKey,
                        String orderNumber, Long amount, Long balance, LocalDateTime createdAt) {
        this.userId = userId;
        this.type = type;
        this.pointKey = pointKey;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public enum PointHistoryType {
        ACCUMULATE,        // 적립
        ACCUMULATE_CANCEL, // 적립 취소
        USE,               // 사용
        USE_CANCEL,        // 사용 취소
        EXPIRE             // 만료
    }
}
