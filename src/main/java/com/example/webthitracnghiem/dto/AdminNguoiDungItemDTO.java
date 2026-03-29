package com.example.webthitracnghiem.dto;

/**
 * Thông tin người dùng trong danh sách quản trị (không chứa mật khẩu).
 */
public class AdminNguoiDungItemDTO {

    private String id;
    private String maNguoiDung;
    private String ho;
    private String ten;
    private String hoTen;
    private String email;
    private String soDienThoai;
    /** Vai trò chính: ADMIN, GIAO_VIEN, SINH_VIEN */
    private String vaiTro;
    /** true: mật khẩu đã băm BCrypt, không thể hiển thị dạng plaintext */
    private boolean matKhauDaBam;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(String maNguoiDung) { this.maNguoiDung = maNguoiDung; }
    public String getHo() { return ho; }
    public void setHo(String ho) { this.ho = ho; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public boolean isMatKhauDaBam() { return matKhauDaBam; }
    public void setMatKhauDaBam(boolean matKhauDaBam) { this.matKhauDaBam = matKhauDaBam; }
}
