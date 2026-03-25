package com.example.webthitracnghiem.dto;

/**
 * DTO - Đối tượng chứa thông tin người dùng trả về sau khi đăng nhập thành công
 * Chỉ chứa các thông tin cần thiết, KHÔNG chứa mật khẩu
 */
public class NguoiDungDTO {

    /**
     * ID duy nhất của người dùng trong hệ thống
     */
    private String id;

    /**
     * Mã người dùng (mã định danh hiển thị)
     */
    private String maNguoiDung;

    /**
     * Họ của người dùng
     */
    private String ho;

    /**
     * Tên của người dùng
     */
    private String ten;

    /**
     * Họ và tên đầy đủ (hợp nhất từ họ và tên)
     */
    private String hoTen;

    /**
     * Email của người dùng
     */
    private String email;

    /**
     * Số điện thoại của người dùng
     */
    private String soDienThoai;

    /**
     * Vai trò của người dùng (ADMIN, GIAO_VIEN, SINH_VIEN)
     */
    private String vaiTro;

    /**
     * Constructor mặc định
     */
    public NguoiDungDTO() {
    }

    /**
     * Constructor với đầy đủ tham số
     */
    public NguoiDungDTO(String id, String maNguoiDung, String ho, String ten,
                        String hoTen, String email, String soDienThoai, String vaiTro) {
        this.id = id;
        this.maNguoiDung = maNguoiDung;
        this.ho = ho;
        this.ten = ten;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.vaiTro = vaiTro;
    }

    // ===== GETTER và SETTER =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
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

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }
}
