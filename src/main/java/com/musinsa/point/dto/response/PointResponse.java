package com.musinsa.point.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "포인트 응답")
public class PointResponse {

    @Schema(description = "포인트 키", example = "ABC123DEF456GHI789")
    private String pointKey;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "적립/사용 금액", example = "1000")
    private Long amount;

    @Schema(description = "사용 가능 잔액", example = "1000")
    private Long remainingAmount;

    @Schema(description = "만료일", example = "2026-11-13T09:29:12")
    private LocalDateTime expirationDate;

    @Schema(description = "수기 지급 여부", example = "false")
    private Boolean isManual;

    @Schema(description = "생성일시", example = "2025-11-13T09:29:12")
    private LocalDateTime createdAt;
}

