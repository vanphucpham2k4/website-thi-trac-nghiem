package com.example.webthitracnghiem.dto;

/**
 * DTO - Phản hồi trả về sau khi đăng nhập thành công.
 * Chứa thông tin người dùng và JWT token.
 */
public class DangNhapResponseDTO {

    /** Thông tin người dùng (không có mật khẩu) */
    private NguoiDungDTO nguoiDung;

    /** Token JWT — lưu ở client để gửi kèm request tiếp theo */
    private String token;

    /**
     * Thời điểm token hết hạn (epoch milliseconds).
     * Frontend dùng để hiển thị countdown / tự động logout khi hết hạn.
     */
    private long expiresAt;

    public DangNhapResponseDTO() {
    }

    public DangNhapResponseDTO(NguoiDungDTO nguoiDung, String token, long expiresAt) {
        this.nguoiDung = nguoiDung;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public NguoiDungDTO getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDungDTO nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
