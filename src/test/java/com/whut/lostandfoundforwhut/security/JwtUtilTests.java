package com.whut.lostandfoundforwhut.security;

import com.whut.lostandfoundforwhut.common.utils.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author DXR
 * @date 2026/01/30
 * @description JWT 工具测试
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class JwtUtilTests {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 生成并解析 Token
     */
    @Test
    void shouldGenerateAndParseToken() {
        String token = jwtUtil.generateToken("test@example.com");
        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("test@example.com", jwtUtil.getEmail(token));
    }
}
