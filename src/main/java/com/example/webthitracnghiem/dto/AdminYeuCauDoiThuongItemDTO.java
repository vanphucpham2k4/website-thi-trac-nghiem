package com.example.webthitracnghiem.dto;

/**
 * Một dòng yêu cầu đổi thưởng trong danh sách admin.
 */
public class AdminYeuCauDoiThuongItemDTO {

    private String id;
    private String maDoi;
    private String trangThai;
    private String thoiGian;
    private int diemDaDung;
    private String tenPhanThuong;
    private String phanThuongId;
    private String sinhVienId;
    private String sinhVienMa;
    private String sinhVienHoTen;
    private String sinhVienEmail;
    private String sinhVienSoDienThoai;

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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }

    public int getDiemDaDung() {
        return diemDaDung;
    }

    public void setDiemDaDung(int diemDaDung) {
        this.diemDaDung = diemDaDung;
    }

    public String getTenPhanThuong() {
        return tenPhanThuong;
    }

    public void setTenPhanThuong(String tenPhanThuong) {
        this.tenPhanThuong = tenPhanThuong;
    }

    public String getPhanThuongId() {
        return phanThuongId;
    }

    public void setPhanThuongId(String phanThuongId) {
        this.phanThuongId = phanThuongId;
    }

    public String getSinhVienId() {
        return sinhVienId;
    }

    public void setSinhVienId(String sinhVienId) {
        this.sinhVienId = sinhVienId;
    }

    public String getSinhVienMa() {
        return sinhVienMa;
    }

    public void setSinhVienMa(String sinhVienMa) {
        this.sinhVienMa = sinhVienMa;
    }

    public String getSinhVienHoTen() {
        return sinhVienHoTen;
    }

    public void setSinhVienHoTen(String sinhVienHoTen) {
        this.sinhVienHoTen = sinhVienHoTen;
    }

    public String getSinhVienEmail() {
        return sinhVienEmail;
    }

    public void setSinhVienEmail(String sinhVienEmail) {
        this.sinhVienEmail = sinhVienEmail;
    }

    public String getSinhVienSoDienThoai() {
        return sinhVienSoDienThoai;
    }

    public void setSinhVienSoDienThoai(String sinhVienSoDienThoai) {
        this.sinhVienSoDienThoai = sinhVienSoDienThoai;
    }
}
