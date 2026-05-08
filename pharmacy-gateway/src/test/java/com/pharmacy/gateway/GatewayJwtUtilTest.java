package com.pharmacy.gateway;

import com.pharmacy.gateway.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayJwtUtilTest {

    private static final String SECRET =
        "cGhhcm1hY3ktc3VwZXItc2VjcmV0LWtleS0yMDI0LXNwcmluZy1ib290LWp3dA==";

    @Test
    void extractUsernameAndValidateToken_ShouldReturnExpectedValues() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        String token = createToken("gateway@pharmacy.com",
            new Date(System.currentTimeMillis() + 60_000));

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("gateway@pharmacy.com");
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalseForExpiredOrInvalidTokens() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        String expiredToken = createToken("expired@pharmacy.com",
            new Date(System.currentTimeMillis() - 1_000));

        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
        assertThat(jwtUtil.validateToken("bad-token")).isFalse();
        assertThatThrownBy(() -> jwtUtil.extractExpiration(expiredToken))
            .isInstanceOf(Exception.class);
    }

    private String createToken(String subject, Date expiration) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Jwts.builder()
            .subject(subject)
            .issuedAt(new Date())
            .expiration(expiration)
            .signWith(Keys.hmacShaKeyFor(keyBytes))
            .compact();
    }
}
