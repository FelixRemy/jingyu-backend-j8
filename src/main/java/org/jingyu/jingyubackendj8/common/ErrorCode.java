package org.jingyu.jingyubackendj8.common;

/**
 * 业务错误码：HTTP 仍返回 200，前端读 body.code
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    ACCOUNT_NOT_EXIST(40001, "账号不存在"),
    PASSWORD_ERROR(40002, "密码错误"),
    NOT_LOGIN(40100, "未登录"),
    TOKEN_EXPIRE(40101, "登录已过期或已下线"),
    TOKEN_INVALID(40102, "token非法，请重新登录"),
    SYSTEM_ERROR(50000, "系统内部异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
