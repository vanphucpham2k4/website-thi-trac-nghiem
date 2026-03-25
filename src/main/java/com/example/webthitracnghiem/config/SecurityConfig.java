package com.example.webthitracnghiem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cấu hình bảo mật — chỉ dùng BCrypt để mã hóa mật khẩu.
 * Không bật Spring Security filter chain (vì project dùng custom auth session).
 */
@Configuration
public class SecurityConfig {

    /**
     * Bean BCryptPasswordEncoder.
     * Dùng encode(rawPassword) để mã hóa khi lưu.
     * Dùng matches(rawPassword, encoded) để kiểm tra khi đăng nhập.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
