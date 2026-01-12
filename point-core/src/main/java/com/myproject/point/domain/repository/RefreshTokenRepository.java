package com.myproject.point.domain.repository;

import com.myproject.point.domain.entity.Member;
import com.myproject.point.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByMember(Member member);
}
