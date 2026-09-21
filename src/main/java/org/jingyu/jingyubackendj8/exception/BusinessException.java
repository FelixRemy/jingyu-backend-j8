package org.jingyu.jingyubackendj8.exception;

import org.jingyu.jingyubackendj8.common.ErrorCode;

/**
 * 可预期业务失败，由 GlobalExceptionHandler 转成统一响应
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
