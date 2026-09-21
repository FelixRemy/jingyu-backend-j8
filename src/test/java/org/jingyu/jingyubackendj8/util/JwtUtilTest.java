package org.jingyu.jingyubackendj8.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        String secret = io.jsonwebtoken.io.Encoders.BASE64.encode(
                Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expire", 3600L);
    }

    @Test
    void generateToken_containsClaims() {
        String token = jwtUtil.generateToken(1L, "alice", "admin");

        assertEquals(1L, ((Number) jwtUtil.getClaims(token).get("userId")).longValue());
        assertEquals("alice", jwtUtil.getClaims(token).get("userAccount", String.class));
        assertEquals("admin", jwtUtil.getClaims(token).get("userRole", String.class));
        assertFalse(jwtUtil.isExpire(token));
        assertEquals(3600L, jwtUtil.getExpire());
    }

    @Test
    void tamperedToken_throwsSignatureException() {
        String token = jwtUtil.generateToken(1L, "alice", "user");
        String tampered = token.substring(0, token.length() - 4) + "xxxx";

        assertThrows(SignatureException.class, () -> jwtUtil.getClaims(tampered));
    }

    @Test
    void expiredToken_parserThrows() {
        ReflectionTestUtils.setField(jwtUtil, "expire", -1L);
        String token = jwtUtil.generateToken(1L, "alice", "user");

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.getClaims(token));
    }
}
