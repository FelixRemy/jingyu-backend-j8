package org.jingyu.jingyubackendj8.exception;

import org.jingyu.jingyubackendj8.common.BaseResponse;
import org.jingyu.jingyubackendj8.common.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessException_usesErrorCode() {
        BaseResponse<?> resp = handler.handleBusinessException(
                new BusinessException(ErrorCode.PASSWORD_ERROR));

        assertEquals(ErrorCode.PASSWORD_ERROR.getCode(), resp.getCode());
        assertEquals(ErrorCode.PASSWORD_ERROR.getMessage(), resp.getMessage());
        assertNull(resp.getData());
    }

    @Test
    void businessException_customMessage_overridesDefault() {
        BaseResponse<?> resp = handler.handleBusinessException(
                new BusinessException(ErrorCode.TOKEN_EXPIRE, "token已过期"));

        assertEquals(ErrorCode.TOKEN_EXPIRE.getCode(), resp.getCode());
        assertEquals("token已过期", resp.getMessage());
    }

    @Test
    void uncaughtException_mapsToSystemError() {
        BaseResponse<?> resp = handler.handleException(new IllegalStateException("boom"));

        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), resp.getCode());
        assertEquals(ErrorCode.SYSTEM_ERROR.getMessage(), resp.getMessage());
    }
}
