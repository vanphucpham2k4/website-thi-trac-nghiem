package com.example.webthitracnghiem.dto;

/**
 * Một dòng lịch sử / chi tiết yêu cầu đổi thưởng.
 */
public class YeuCauDoiThuongDTO {

    private String id;
    private String maDoi;
    private String phanThuongId;
    private String tenPhanThuong;
    private String moTaNgan;
    private int diemDaDung;
    private String thoiGian;
    private String trangThai;
    private String ghiChu;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaDoi() {
        return maDoi;
    }

    public void setMaDoi(String maDoi) {
        this.maDoi = maDoi;
    }

    public String getPhanThuongId() {
        return phanThuongId;
    }

    public void setPhanThuongId(String phanThuongId) {
        this.phanThuongId = phanThuongId;
    }

    public String getTenPhanThuong() {
        return tenPhanThuong;
    }

    public void setTenPhanThuong(String tenPhanThuong) {
        this.tenPhanThuong = tenPhanThuong;
    }

    public String getMoTaNgan() {
        return moTaNgan;
    }

    public void setMoTaNgan(String moTaNgan) {
        this.moTaNgan = moTaNgan;
    }

    public int getDiemDaDung() {
        return diemDaDung;
    }

    public void setDiemDaDung(int diemDaDung) {
        this.diemDaDung = diemDaDung;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
