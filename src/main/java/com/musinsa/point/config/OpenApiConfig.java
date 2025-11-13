package com.musinsa.point.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("무신사 포인트 시스템 API")
                        .description("무료 포인트 시스템 API 문서")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Musinsa Payment")
                                .email("payment@musinsa.com")));
    }
}

