package com.musinsa.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포인트 사용 요청")
public class UseRequest {

    @Schema(description = "사용자 ID", example = "1", required = true)
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @Schema(description = "주문번호", example = "A1234", required = true)
    @NotBlank(message = "주문번호는 필수입니다.")
    private String orderNumber;

    @Schema(description = "사용 금액", example = "1200", required = true)
    @NotNull(message = "사용 금액은 필수입니다.")
    @Min(value = 1, message = "사용 금액은 1원 이상이어야 합니다.")
    private Long amount;
}