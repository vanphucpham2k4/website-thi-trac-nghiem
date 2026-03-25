package com.example.webthitracnghiem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO - Đối tượng truyền dữ liệu cho yêu cầu ĐĂNG KÝ tài khoản mới
 * Chứa các trường thông tin cần thiết khi người dùng đăng ký tài khoản
 * Sử dụng Jakarta Validation để kiểm tra dữ liệu đầu vào
 */
public class DangKyDTO {

    /**
     * Email của người dùng - Dùng để đăng nhập và nhận thông báo
     * Validation: không được trống, phải đúng định dạng email
     */
    @NotBlank(message = "Email không được trống")
    private String email;

    /**
     * Họ của người dùng (họ + tên đệm)
     * Validation: không được trống
     */
    @NotBlank(message = "Họ không được trống")
    private String ho;

    /**
     * Tên của người dùng (tên chính)
     * Validation: không được trống
     */
    @NotBlank(message = "Tên không được trống")
    private String ten;

    /**
     * Số điện thoại của người dùng
     * Validation: không được trống
     */
    @NotBlank(message = "Số điện thoại không được trống")
    private String soDienThoai;

    /**
     * Mật khẩu đăng nhập
     * Validation: không được trống
     */
    @NotBlank(message = "Mật khẩu không được trống")
    private String matKhau;

    /**
     * Vai trò người dùng muốn đăng ký: "SINH_VIEN" hoặc "GIAO_VIEN"
     * Validation: không được trống, phải là 1 trong 2 giá trị
     * - SINH_VIEN: Tài khoản dành cho học sinh/sinh viên
     * - GIAO_VIEN: Tài khoản dành cho giáo viên
     */
    @NotBlank(message = "Vai trò không được trống")
    private String vaiTro;

    /**
     * Câu trả lời CAPTCHA của người dùng
     * Validation: không được trống, phải khớp với kết quả tính toán
     */
    @NotNull(message = "CAPTCHA không được trống")
    private Integer captchaAnswer;

    /**
     * ID CAPTCHA để xác thực (phòng chống replay attack)
     */
    @NotBlank(message = "Mã CAPTCHA không hợp lệ")
    private String captchaId;

    // ===== GETTER và SETTER =====
    // Các phương thức getter và setter để truy cập/cập nhật các thuộc tính

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
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
}
