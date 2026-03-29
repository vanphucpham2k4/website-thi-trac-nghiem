package com.example.webthitracnghiem.config;

import com.example.webthitracnghiem.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter xác thực JWT trong Authorization header của mọi request.
 * Chỉ áp dụng cho các request API (bắt đầu bằng /api/).
 * Nếu header có "Authorization: Bearer <token>", token được gắn vào request attribute
 * để controller/service đọc và kiểm tra.
 * Không chặn request thiếu token (cho phép login/register không cần token).
 */
@Component
@Order(1)
public class JwtAuthFilter implements Filter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request  = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();

        // Chỉ xử lý API requests
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (jwtService.kiemTraToken(token)) {
                // Token hợp lệ → gắn vào request attribute để controller đọc
                request.setAttribute("jwtToken", token);
                request.setAttribute("jwtUserId", jwtService.layUserIdTuToken(token));
                request.setAttribute("jwtVaiTro", jwtService.layVaiTroTuToken(token));
            }
            // Token sai/hết hạn → không gắn attribute, controller sẽ kiểm tra và trả lỗi
        }

        chain.doFilter(request, response);
    }
}
