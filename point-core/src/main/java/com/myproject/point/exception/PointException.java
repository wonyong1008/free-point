package com.myproject.point.exception;

import lombok.Getter;

@Getter
public class PointException extends RuntimeException {

    private final ErrorCode errorCode;

    public PointException(ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode, args));
        this.errorCode = errorCode;
    }

    private static String formatMessage(ErrorCode errorCode, Object... args) {
        if (args == null || args.length == 0) {
            return errorCode.getMessage();
        }
        return String.format(errorCode.getMessage(), args);
    }
}
