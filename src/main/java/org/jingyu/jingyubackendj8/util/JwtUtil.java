package org.jingyu.jingyubackendj8.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Colin
 */
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    public long getExpire() {
        return expire;
    }

    // 统一获取密钥：secret配置文件中存放base64字符串
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String userAccount, String userRole) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("userAccount", userAccount);
        map.put("userRole", userRole);

        return Jwts.builder()
                .setClaims(map)
                .setExpiration(new Date(System.currentTimeMillis() + expire * 1000))
                // 使用SecretKey对象，不要传字符串！
                .signWith(getSecretKey())
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //解析 JWT，拿到它的过期时间，判断：这个 token 的过期时间，是不是已经早于服务器现在的时间。
    public boolean isExpire(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // 运行这个main方法，打印输出base64密钥，复制到yml
    public static void main(String[] args) {
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Secret = io.jsonwebtoken.io.Encoders.BASE64.encode(secretKey.getEncoded());
        System.out.println(base64Secret);
    }

}