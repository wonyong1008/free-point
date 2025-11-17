package com.musinsa.point.service;

import com.musinsa.point.domain.repository.MemberRepository;
import com.musinsa.point.domain.repository.RefreshTokenRepository;
import com.musinsa.point.dto.request.LoginRequest;
import com.musinsa.point.dto.request.SignupRequest;
import com.musinsa.point.dto.request.TokenRefreshRequest;
import com.musinsa.point.dto.response.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 후 로그인 및 리프레시 토큰 재발급")
    void signup_login_refresh() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("user@example.com");
        signupRequest.setPassword("password123");
        authService.signup(signupRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password123");
        TokenResponse loginResponse = authService.login(loginRequest);

        assertThat(loginResponse.getAccessToken()).isNotBlank();
        assertThat(loginResponse.getRefreshToken()).isNotBlank();

        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        refreshRequest.setRefreshToken(loginResponse.getRefreshToken());
        TokenResponse refreshResponse = authService.refresh(refreshRequest);

        assertThat(refreshResponse.getAccessToken()).isNotBlank();
        assertThat(refreshResponse.getRefreshToken()).isNotBlank();
    }
}
