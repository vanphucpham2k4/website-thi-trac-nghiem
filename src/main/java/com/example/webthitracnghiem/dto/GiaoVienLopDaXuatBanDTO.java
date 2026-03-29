package com.example.webthitracnghiem.dto;

/**
 * Lớp đã xuất bản đề thi — hiển thị trong modal Thu hồi.
 */
public class GiaoVienLopDaXuatBanDTO {

    private String id;
    private String tenLop;
    /** ISO local date-time */
    private String thoiGianXuatBan;
    /** Số sinh viên trong lớp */
    private long soSinhVien;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public String getThoiGianXuatBan() {
        return thoiGianXuatBan;
    }

    public void setThoiGianXuatBan(String thoiGianXuatBan) {
        this.thoiGianXuatBan = thoiGianXuatBan;
    }

    public long getSoSinhVien() {
        return soSinhVien;
    }

    public void setSoSinhVien(long soSinhVien) {
        this.soSinhVien = soSinhVien;
    }
}
