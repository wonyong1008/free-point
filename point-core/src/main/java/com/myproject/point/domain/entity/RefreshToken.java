package com.myproject.point.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public RefreshToken(Member member, String token, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.member = member;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }
}
