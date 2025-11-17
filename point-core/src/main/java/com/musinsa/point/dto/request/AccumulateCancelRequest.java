package com.musinsa.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포인트 적립 취소 요청")
public class AccumulateCancelRequest {

    @Schema(description = "포인트 키 (적립 시 발급된 키)", example = "ABC123DEF456GHI789", required = true)
    @NotBlank(message = "포인트 키는 필수입니다.")
    private String pointKey;
}