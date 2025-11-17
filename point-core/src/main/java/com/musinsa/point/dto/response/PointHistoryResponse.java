package com.musinsa.point.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "포인트 이력 응답")
public class PointHistoryResponse {

    @Schema(description = "이력 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이력 유형 (ACCUMULATE, ACCUMULATE_CANCEL, USE, USE_CANCEL, EXPIRE)", example = "ACCUMULATE")
    private String type;

    @Schema(description = "포인트 키", example = "ABC123DEF456GHI789")
    private String pointKey;

    @Schema(description = "주문번호 (사용/사용취소 시)", example = "A1234")
    private String orderNumber;

    @Schema(description = "금액", example = "1000")
    private Long amount;

    @Schema(description = "거래 후 잔액", example = "1000")
    private Long balance;

    @Schema(description = "생성일시", example = "2025-11-13T09:29:12")
    private LocalDateTime createdAt;
}
