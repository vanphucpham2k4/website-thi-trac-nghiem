package com.example.webthitracnghiem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO - Đối tượng truyền dữ liệu cho yêu cầu ĐĂNG NHẬP
 * Chứa thông tin đăng nhập của người dùng
 * Hỗ trợ đăng nhập bằng email HOẶC số điện thoại
 */
public class DangNhapDTO {

    /**
     * Tài khoản đăng nhập - có thể là email HOẶC số điện thoại
     * Validation: không được trống
     */
    @NotBlank(message = "Tài khoản không được trống")
    private String taiKhoan;

    /**
     * Mật khẩu đăng nhập
     * Validation: không được trống
     */
    @NotBlank(message = "Mật khẩu không được trống")
    private String matKhau;

    /**
     * Câu trả lời CAPTCHA của người dùng
     * Validation: không được trống
     */
    @NotNull(message = "CAPTCHA không được trống")
    private Integer captchaAnswer;

    /**
     * ID CAPTCHA để xác thực (phòng chống replay attack)
     */
    @NotBlank(message = "Mã CAPTCHA không hợp lệ")
    private String captchaId;

    /**
     * Ghi nhớ đăng nhập.
     * true  = lưu token vào localStorage  (30 ngày — dùng refresh token)
     * false = lưu token vào sessionStorage (hết tab = mất, token vẫn hết hạn 30p)
     *
     * Hiện tại chỉ ảnh hưởng nơi lưu trữ ở client; token server vẫn hết hạn 30 phút.
     */
    private boolean ghiNho = false;

    // ===== GETTER và SETTER =====

    public String getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(String taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public Integer getCaptchaAnswer() {
        return captchaAnswer;
    }

    public void setCaptchaAnswer(Integer captchaAnswer) {
        this.captchaAnswer = captchaAnswer;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public boolean isGhiNho() {
        return ghiNho;
    }

    public void setGhiNho(boolean ghiNho) {
        this.ghiNho = ghiNho;
    }
}
