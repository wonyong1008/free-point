package com.musinsa.point.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "point")
public class PointProperties {

    private final Expiration expiration = new Expiration();

    @Getter
    @Setter
    public static class Expiration {
        /**
         * 만료 배치 실행 주기 (cron)
         */
        private String cron = "0 0 * * * *";
        /**
         * 한번에 처리할 만료 포인트 개수
         */
        private int batchSize = 500;
    }
}
