package com.example.webthitracnghiem.dto;

/**
 * Một dòng bảng Quản lý lớp học (giáo viên).
 */
public class GiaoVienLopHocListItemDTO {

    private String id;
    private String tenLop;
    private long soSinhVien;
    private String tenChuTri;
    /** ISO-8601 local date-time */
    private String thoiGianTao;

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

    public long getSoSinhVien() {
        return soSinhVien;
    }

    public void setSoSinhVien(long soSinhVien) {
        this.soSinhVien = soSinhVien;
    }

    public String getTenChuTri() {
        return tenChuTri;
    }

    public void setTenChuTri(String tenChuTri) {
        this.tenChuTri = tenChuTri;
    }

    public String getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(String thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
