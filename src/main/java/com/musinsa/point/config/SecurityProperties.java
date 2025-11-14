package com.musinsa.point.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Jwt {
        /**
         * HMAC 서명에 사용할 시크릿 키
         */
        private String secret;

        /**
         * Access Token 유효기간
         */
        private Duration accessTokenValidity = Duration.ofMinutes(30);

        /**
         * Refresh Token 유효기간
         */
        private Duration refreshTokenValidity = Duration.ofDays(14);
    }
}
