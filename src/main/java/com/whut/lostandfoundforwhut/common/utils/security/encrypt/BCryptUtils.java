package com.whut.lostandfoundforwhut.common.utils.security.encrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author DXR
 * @date 2026/01/30
 * @description BCrypt 工具类（不可逆哈希）
 */
public class BCryptUtils {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private BCryptUtils() {
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 加密（哈希）
     * @param rawPassword 明文密码
     * @return 哈希后的密码
     */
    public static String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 校验密码是否匹配
     * @param rawPassword 明文密码
     * @param hashedPassword 哈希密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String hashedPassword) {
        return ENCODER.matches(rawPassword, hashedPassword);
    }
}
