package com.musinsa.point.service;

import com.musinsa.point.config.SecurityProperties;
import com.musinsa.point.domain.entity.Member;
import com.musinsa.point.domain.entity.RefreshToken;
import com.musinsa.point.domain.repository.MemberRepository;
import com.musinsa.point.domain.repository.RefreshTokenRepository;
import com.musinsa.point.dto.request.LoginRequest;
import com.musinsa.point.dto.request.SignupRequest;
import com.musinsa.point.dto.request.TokenRefreshRequest;
import com.musinsa.point.dto.response.TokenResponse;
import com.musinsa.point.exception.ErrorCode;
import com.musinsa.point.exception.PointException;
import com.musinsa.point.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityProperties securityProperties;
    private final Clock clock;

    @Transactional
    public void signup(SignupRequest request) {
        memberRepository.findByEmail(request.getEmail())
                .ifPresent(member -> {
                    throw new PointException(ErrorCode.MEMBER_ALREADY_EXISTS);
                });

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Member.Role.USER)
                .createdAt(LocalDateTime.now(clock))
                .build();
        memberRepository.save(member);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new PointException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new PointException(ErrorCode.INVALID_CREDENTIALS);
        }

        refreshTokenRepository.deleteByMember(member);
        return issueTokens(member);
    }

    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new PointException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(clock);
        if (refreshToken.isExpired(now)) {
            refreshTokenRepository.delete(refreshToken);
            throw new PointException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        Member member = refreshToken.getMember();
        refreshTokenRepository.delete(refreshToken);
        return issueTokens(member);
    }

    private TokenResponse issueTokens(Member member) {
        String accessToken = jwtTokenProvider.generateAccessToken(member);
        String refreshTokenValue = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now(clock);
        RefreshToken refreshToken = RefreshToken.builder()
                .member(member)
                .token(refreshTokenValue)
                .expiresAt(now.plus(securityProperties.getJwt().getRefreshTokenValidity()))
                .createdAt(now)
                .build();
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .expiresIn(securityProperties.getJwt().getAccessTokenValidity().toSeconds())
                .build();
    }
}
