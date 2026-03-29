package com.example.webthitracnghiem.dto;

/**
 * DTO — Một đề thi đã xuất bản cho lớp, dùng trong Bước 2 "Xem kết quả".
 */
public class GiaoVienKetQuaDeThiItemDTO {

    private String deThiId;
    private String maDeThi;
    private String tenDeThi;
    private String tenMonHoc;
    private Integer thoiGianPhut;
    private long soLuotThi;

    public String getDeThiId() { return deThiId; }
    public void setDeThiId(String deThiId) { this.deThiId = deThiId; }

    public String getMaDeThi() { return maDeThi; }
    public void setMaDeThi(String maDeThi) { this.maDeThi = maDeThi; }

    public String getTenDeThi() { return tenDeThi; }
    public void setTenDeThi(String tenDeThi) { this.tenDeThi = tenDeThi; }

    public String getTenMonHoc() { return tenMonHoc; }
    public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }

    public Integer getThoiGianPhut() { return thoiGianPhut; }
    public void setThoiGianPhut(Integer thoiGianPhut) { this.thoiGianPhut = thoiGianPhut; }

    public long getSoLuotThi() { return soLuotThi; }
    public void setSoLuotThi(long soLuotThi) { this.soLuotThi = soLuotThi; }

    /** Số lượt thi ẩn danh (qua link công khai, lopHoc = null) */
    private long soLuotThiAnDanh;
    public long getSoLuotThiAnDanh() { return soLuotThiAnDanh; }
    public void setSoLuotThiAnDanh(long soLuotThiAnDanh) { this.soLuotThiAnDanh = soLuotThiAnDanh; }
}
