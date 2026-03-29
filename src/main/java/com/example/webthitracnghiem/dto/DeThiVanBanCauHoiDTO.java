package com.example.webthitracnghiem.dto;

import java.math.BigDecimal;

/**
 * Trang chỉnh sửa văn bản thô — nội dung câu hỏi trong đề.
 */
public class DeThiVanBanCauHoiDTO {

    private String deThiId;
    private String tenDeThi;
    private String maDeThi;
    private String trangThai;
    private String tenMonHoc;
    private int soCau;
    /** Tổng điểm bài thi (mặc định 10 nếu chưa cấu hình). */
    private BigDecimal thangDiemToiDa;
    /** Điểm mỗi câu = thangDiemToiDa / soCau (làm tròn). */
    private BigDecimal diemMoiCau;
    private String vanBan;

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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public int getSoCau() {
        return soCau;
    }

    public void setSoCau(int soCau) {
        this.soCau = soCau;
    }

    public BigDecimal getThangDiemToiDa() {
        return thangDiemToiDa;
    }

    public void setThangDiemToiDa(BigDecimal thangDiemToiDa) {
        this.thangDiemToiDa = thangDiemToiDa;
    }

    public BigDecimal getDiemMoiCau() {
        return diemMoiCau;
    }

    public void setDiemMoiCau(BigDecimal diemMoiCau) {
        this.diemMoiCau = diemMoiCau;
    }

    public String getVanBan() {
        return vanBan;
    }

    public void setVanBan(String vanBan) {
        this.vanBan = vanBan;
    }
}
