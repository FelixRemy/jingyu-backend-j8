package org.jingyu.jingyubackendj8.interceptor;


import com.alibaba.fastjson.JSON;
import org.jingyu.jingyubackendj8.common.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jingyu.jingyubackendj8.util.JwtUtil;
import org.jingyu.jingyubackendj8.util.RedisUtil;


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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.contains("/user/login") || uri.contains("/user/register")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            fail(response, "未登录");
            return false;
        }
        token = token.substring(7);

        String redisKey = "token:" + token;
        if (redisUtil.get(redisKey) == null) {
            fail(response, "登录已过期或已下线");
            return false;
        }

        // =========完整捕获JWT全部异常=========
        try {
            if (jwtUtil.isExpire(token)) {
                fail(response, "token已过期");
                return false;
            }
        } catch (io.jsonwebtoken.security.SignatureException e) {
            //签名不匹配：密钥对不上、token被篡改
            fail(response, "token非法，请重新登录");
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            fail(response, "token已过期，请重新登录");
            return false;
        } catch (Exception e) {
            fail(response, "token解析失败，请重新登录");
            return false;
        }
        return true;
    }

    private void fail(HttpServletResponse response, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(ResultUtil.error(401, msg)));
    }
}
