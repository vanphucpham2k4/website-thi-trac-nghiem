package com.example.webthitracnghiem.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service xử lý JWT (JSON Web Token).
 * Cung cấp: tạo token, xác thực token, trích xuất thông tin từ token.
 * Token có thời hạn 30 phút (cấu hình trong application.properties).
 */
@Service
public class JwtService {

    /** Khóa bí mật ký JWT — được đọc từ application.properties */
    private final SecretKey secretKey;

    /** Thời gian hiệu lực token (mili giây) — mặc định 30 phút */
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        // Đảm bảo secret có đủ bytes cho HS256 (≥ 256-bit / 32 bytes)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Padding nếu secret quá ngắn (không nên xảy ra với giá trị hiện tại)
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Tạo JWT token cho người dùng.
     *
     * @param userId  ID người dùng (subject của token)
     * @param email   Email người dùng (claim tùy chỉnh)
     * @param vaiTro  Vai trò người dùng (claim tùy chỉnh)
     * @return Chuỗi JWT token đã ký
     */
    public String taoToken(String userId, String email, String vaiTro) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)                              // userId làm subject
                .claim("email", email)                        // lưu email trong payload
                .claim("vaiTro", vaiTro)                      // lưu vai trò trong payload
                .issuedAt(now)                                // thời điểm tạo
                .expiration(expiryDate)                       // thời điểm hết hạn
                .signWith(secretKey, Jwts.SIG.HS256)         // ký bằng HS256
                .compact();
    }

    /**
     * Lấy userId (subject) từ token.
     *
     * @param token Chuỗi JWT
     * @return userId hoặc null nếu token không hợp lệ
     */
    public String layUserIdTuToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Lấy email từ token.
     *
     * @param token Chuỗi JWT
     * @return email hoặc null nếu không có
     */
    public String layEmailTuToken(String token) {
        try {
            return parseClaims(token).get("email", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Lấy vai trò từ token.
     *
     * @param token Chuỗi JWT
     * @return vaiTro hoặc null nếu không có
     */
    public String layVaiTroTuToken(String token) {
        try {
            return parseClaims(token).get("vaiTro", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * Lấy thời điểm hết hạn từ token (timestamp milliseconds).
     *
     * @param token Chuỗi JWT
     * @return Thời điểm hết hạn (epoch ms), hoặc -1 nếu lỗi
     */
    public long layThoiDiemHetHan(String token) {
        try {
            Date exp = parseClaims(token).getExpiration();
            return exp != null ? exp.getTime() : -1;
        } catch (JwtException e) {
            return -1;
        }
    }

    /**
     * Kiểm tra token còn hiệu lực không.
     *
     * @param token Chuỗi JWT
     * @return true nếu token hợp lệ và chưa hết hạn; false nếu hết hạn hoặc không hợp lệ
     */
    public boolean kiemTraToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false; // Token đã hết hạn
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Token không hợp lệ (sai chữ ký, format sai…)
        }
    }

    /**
     * Parse và trả về Claims từ token.
     * Nếu token không hợp lệ sẽ ném ngoại lệ.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
