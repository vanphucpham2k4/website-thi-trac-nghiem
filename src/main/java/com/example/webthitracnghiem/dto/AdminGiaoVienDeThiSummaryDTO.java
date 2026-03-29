package com.example.webthitracnghiem.dto;

/**
 * DTO — Tóm tắt thống kê đề thi của một giảng viên (dùng cho Admin cấp 1).
 */
public class AdminGiaoVienDeThiSummaryDTO {

    private String nguoiDungId;
    private String hoTen;
    private String email;
    private long tongDeThi;
    private long soDeThiNhap;
    private long soDeThiCongKhai;
    private long soMonHoc;

    public String getNguoiDungId() { return nguoiDungId; }
    public void setNguoiDungId(String nguoiDungId) { this.nguoiDungId = nguoiDungId; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getTongDeThi() { return tongDeThi; }
    public void setTongDeThi(long tongDeThi) { this.tongDeThi = tongDeThi; }

    public long getSoDeThiNhap() { return soDeThiNhap; }
    public void setSoDeThiNhap(long soDeThiNhap) { this.soDeThiNhap = soDeThiNhap; }

    public long getSoDeThiCongKhai() { return soDeThiCongKhai; }
    public void setSoDeThiCongKhai(long soDeThiCongKhai) { this.soDeThiCongKhai = soDeThiCongKhai; }

    public long getSoMonHoc() { return soMonHoc; }
    public void setSoMonHoc(long soMonHoc) { this.soMonHoc = soMonHoc; }
}
