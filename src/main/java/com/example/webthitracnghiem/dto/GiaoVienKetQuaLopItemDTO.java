package com.example.webthitracnghiem.dto;

/**
 * DTO — Một lớp trong danh sách "Xem kết quả" của giáo viên (Bước 1).
 */
public class GiaoVienKetQuaLopItemDTO {

    private String id;
    private String tenLop;
    private long soSinhVien;
    private long soDeThiXuatBan;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public long getSoSinhVien() { return soSinhVien; }
    public void setSoSinhVien(long soSinhVien) { this.soSinhVien = soSinhVien; }

    public long getSoDeThiXuatBan() { return soDeThiXuatBan; }
    public void setSoDeThiXuatBan(long soDeThiXuatBan) { this.soDeThiXuatBan = soDeThiXuatBan; }
}
