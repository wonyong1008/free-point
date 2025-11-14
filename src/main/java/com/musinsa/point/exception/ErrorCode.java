package com.musinsa.point.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    EXCEED_MAX_ACCUMULATE(HttpStatus.BAD_REQUEST, "POINT-001", "1회 최대 적립 가능 포인트는 %s원입니다."),
    EXCEED_MAX_BALANCE(HttpStatus.BAD_REQUEST, "POINT-002", "개인별 최대 보유 포인트는 %s원입니다."),
    INVALID_EXPIRATION_DAYS(HttpStatus.BAD_REQUEST, "POINT-003", "만료일은 %s일 이상 %s일 미만이어야 합니다."),
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT-004", "포인트를 찾을 수 없습니다."),
    POINT_ALREADY_USED(HttpStatus.BAD_REQUEST, "POINT-005", "일부 사용된 포인트는 적립 취소할 수 없습니다."),
    NO_AVAILABLE_POINTS(HttpStatus.BAD_REQUEST, "POINT-006", "사용 가능한 포인트가 없습니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "POINT-007", "사용 가능한 포인트가 부족합니다."),
    POINT_USAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT-008", "포인트 사용 내역을 찾을 수 없습니다."),
    CANCEL_AMOUNT_EXCEEDS(HttpStatus.BAD_REQUEST, "POINT-009", "취소 가능한 포인트가 부족합니다."),
    INVALID_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "POINT-010", "취소 금액은 1원 이상이어야 합니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "POINT-999", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
