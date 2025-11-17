package com.musinsa.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포인트 적립 요청")
public class AccumulateRequest {

    @Schema(description = "사용자 ID", example = "1", required = true)
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @Schema(description = "적립 금액 (1원 이상, 1회 최대 적립 가능 금액 이하)", example = "1000", required = true)
    @NotNull(message = "적립 금액은 필수입니다.")
    @Min(value = 1, message = "적립 금액은 1원 이상이어야 합니다.")
    private Long amount;

    @Schema(description = "만료일 (일 단위, 1일 이상 5년 미만, 기본값: 365일)", example = "365")
    private Integer expirationDays;

    @Builder.Default
    @Schema(description = "수기 지급 여부", example = "false")
    private Boolean isManual = false;
}
