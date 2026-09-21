package org.jingyu.jingyubackendj8.interceptor;

import org.jingyu.jingyubackendj8.common.ErrorCode;
import org.jingyu.jingyubackendj8.exception.BusinessException;
import org.jingyu.jingyubackendj8.util.JwtUtil;
import org.jingyu.jingyubackendj8.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Colin
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (uri.contains("/user/login") || uri.contains("/user/register")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        token = token.substring(7);

        String redisKey = "token:" + token;
        if (redisUtil.get(redisKey) == null) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRE);
        }

        try {
            if (jwtUtil.isExpire(token)) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRE, "token已过期");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRE, "token已过期，请重新登录");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "token解析失败，请重新登录");
        }
        return true;
    }
}
