package com.musinsa.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "포인트 사용 취소 요청")
public class UseCancelRequest {

    @Schema(description = "포인트 키 (사용 시 발급된 키)", example = "XYZ789ABC123DEF456", required = true)
    @NotBlank(message = "포인트 키는 필수입니다.")
    private String pointKey;

    @Schema(description = "취소 금액 (미지정 시 전체 취소)", example = "1100")
    @Min(value = 1, message = "취소 금액은 1원 이상이어야 합니다.")
    private Long amount;
}

