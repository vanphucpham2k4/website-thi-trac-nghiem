package com.example.webthitracnghiem.dto;

/**
 * Giáo viên xem bài làm của sinh viên (theo phiên): đang làm hoặc đã nộp.
 */
public class GiaoVienXemBaiThiDTO {

    private boolean daNop;
    private String hoTenSinhVien;
    private String maNguoiDungSinhVien;

    private SinhVienBaiThiDTO noiDungDangLam;
    private SinhVienLichSuChiTietDTO chiTietDaNop;

    public boolean isDaNop() {
        return daNop;
    }

    public void setDaNop(boolean daNop) {
        this.daNop = daNop;
    }

    public String getHoTenSinhVien() {
        return hoTenSinhVien;
    }

    public void setHoTenSinhVien(String hoTenSinhVien) {
        this.hoTenSinhVien = hoTenSinhVien;
    }

    public String getMaNguoiDungSinhVien() {
        return maNguoiDungSinhVien;
    }

    public void setMaNguoiDungSinhVien(String maNguoiDungSinhVien) {
        this.maNguoiDungSinhVien = maNguoiDungSinhVien;
    }

    public SinhVienBaiThiDTO getNoiDungDangLam() {
        return noiDungDangLam;
    }

    public void setNoiDungDangLam(SinhVienBaiThiDTO noiDungDangLam) {
        this.noiDungDangLam = noiDungDangLam;
    }

    public SinhVienLichSuChiTietDTO getChiTietDaNop() {
        return chiTietDaNop;
    }

    public void setChiTietDaNop(SinhVienLichSuChiTietDTO chiTietDaNop) {
        this.chiTietDaNop = chiTietDaNop;
    }
}
