package com.example.webthitracnghiem.dto;

/**
 * DTO cập nhật thông tin hồ sơ sinh viên (không gồm mật khẩu).
 */
public class CapNhatHoSoSinhVienDTO {

    private String ho;
    private String ten;
    private String email;
    private String soDienThoai;

    public String getHo() { return ho; }
    public void setHo(String ho) { this.ho = ho; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
}
