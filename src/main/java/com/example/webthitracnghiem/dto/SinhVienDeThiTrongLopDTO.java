package com.example.webthitracnghiem.dto;

/**
 * Đề thi đã xuất bản trong lớp (sinh viên).
 */
public class SinhVienDeThiTrongLopDTO {

    private String deThiId;
    private String tenDeThi;
    private String maDeThi;
    private Integer thoiGianPhut;
    private long soCauHoi;
    private String tenMonHoc;
    /** ISO local date-time */
    private String thoiGianXuatBan;
    /** Có phiên đang làm dở (chưa nộp) */
    private boolean coPhienDangLam;
    private String phienThiIdDangLam;

    public String getDeThiId() {
        return deThiId;
    }

    public void setDeThiId(String deThiId) {
        this.deThiId = deThiId;
    }

    public String getTenDeThi() {
        return tenDeThi;
    }

    public void setTenDeThi(String tenDeThi) {
        this.tenDeThi = tenDeThi;
    }

    public String getMaDeThi() {
        return maDeThi;
    }

    public void setMaDeThi(String maDeThi) {
        this.maDeThi = maDeThi;
    }

    public Integer getThoiGianPhut() {
        return thoiGianPhut;
    }

    public void setThoiGianPhut(Integer thoiGianPhut) {
        this.thoiGianPhut = thoiGianPhut;
    }

    public long getSoCauHoi() {
        return soCauHoi;
    }

    public void setSoCauHoi(long soCauHoi) {
        this.soCauHoi = soCauHoi;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public String getThoiGianXuatBan() {
        return thoiGianXuatBan;
    }

    public void setThoiGianXuatBan(String thoiGianXuatBan) {
        this.thoiGianXuatBan = thoiGianXuatBan;
    }

    public boolean isCoPhienDangLam() {
        return coPhienDangLam;
    }

    public void setCoPhienDangLam(boolean coPhienDangLam) {
        this.coPhienDangLam = coPhienDangLam;
    }

    public String getPhienThiIdDangLam() {
        return phienThiIdDangLam;
    }

    public void setPhienThiIdDangLam(String phienThiIdDangLam) {
        this.phienThiIdDangLam = phienThiIdDangLam;
    }
}
