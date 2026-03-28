package com.example.webthitracnghiem.dto;

/**
 * Một dòng trong bảng Quản lý sinh viên (giao viên).
 */
public class GiaoVienSinhVienListItemDTO {

    private String id;
    private String maNguoiDung;
    private String hoTen;
    private String email;
    private String soDienThoai;
    /** Số phiên thi trên các đề do giáo viên này tạo (đề chưa xóa mềm). */
    private long soLuotThiVoiGiaoVien;

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

    public long getSoLuotThiVoiGiaoVien() {
        return soLuotThiVoiGiaoVien;
    }

    public void setSoLuotThiVoiGiaoVien(long soLuotThiVoiGiaoVien) {
        this.soLuotThiVoiGiaoVien = soLuotThiVoiGiaoVien;
    }
}
