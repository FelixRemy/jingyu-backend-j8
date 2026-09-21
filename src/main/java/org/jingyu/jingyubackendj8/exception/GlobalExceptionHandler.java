package org.jingyu.jingyubackendj8.exception;

import lombok.extern.slf4j.Slf4j;
import org.jingyu.jingyubackendj8.common.BaseResponse;
import org.jingyu.jingyubackendj8.common.ErrorCode;
import org.jingyu.jingyubackendj8.common.ResultUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一把异常转成 BaseResponse，避免每个接口自己 return error
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常 code={}, msg={}", e.getCode(), e.getMessage());
        return ResultUtil.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e) {
        log.error("未捕获异常", e);
        return ResultUtil.error(ErrorCode.SYSTEM_ERROR);
    }
}
