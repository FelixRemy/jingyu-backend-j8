package org.jingyu.jingyubackendj8.interceptor;

import org.jingyu.jingyubackendj8.common.ErrorCode;
import org.jingyu.jingyubackendj8.exception.BusinessException;
import org.jingyu.jingyubackendj8.util.JwtUtil;
import org.jingyu.jingyubackendj8.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private TokenInterceptor interceptor;

    @Test
    void loginAndRegister_skipAuth() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/user/login");
        request.setRequestURI("/api/user/login");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void missingHeader_throwsNotLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/user/logout");
        request.setRequestURI("/api/user/logout");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals(ErrorCode.NOT_LOGIN.getCode(), ex.getCode());
    }

    @Test
    void redisMiss_throwsTokenExpire() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/user/logout");
        request.setRequestURI("/api/user/logout");
        request.addHeader("Authorization", "Bearer abc");
        when(redisUtil.get("token:abc")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals(ErrorCode.TOKEN_EXPIRE.getCode(), ex.getCode());
    }

    @Test
    void validToken_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/user/logout");
        request.setRequestURI("/api/user/logout");
        request.addHeader("Authorization", "Bearer abc");
        when(redisUtil.get("token:abc")).thenReturn("ok");
        when(jwtUtil.isExpire("abc")).thenReturn(false);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
}
