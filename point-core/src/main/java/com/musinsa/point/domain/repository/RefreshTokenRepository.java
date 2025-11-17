package com.musinsa.point.domain.repository;

import com.musinsa.point.domain.entity.Member;
import com.musinsa.point.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByMember(Member member);
}
