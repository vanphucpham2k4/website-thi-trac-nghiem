package com.example.webthitracnghiem.dto;

/**
 * DTO — Tóm tắt thống kê câu hỏi của một giảng viên (dùng cho Admin cấp 1).
 */
public class AdminGiaoVienCauHoiSummaryDTO {

    private String nguoiDungId;
    private String hoTen;
    private String email;
    private long tongCauHoi;
    private long soCauDe;
    private long soCauTrungBinh;
    private long soCauKho;

    public String getNguoiDungId() { return nguoiDungId; }
    public void setNguoiDungId(String nguoiDungId) { this.nguoiDungId = nguoiDungId; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getTongCauHoi() { return tongCauHoi; }
    public void setTongCauHoi(long tongCauHoi) { this.tongCauHoi = tongCauHoi; }

    public long getSoCauDe() { return soCauDe; }
    public void setSoCauDe(long soCauDe) { this.soCauDe = soCauDe; }

    public long getSoCauTrungBinh() { return soCauTrungBinh; }
    public void setSoCauTrungBinh(long soCauTrungBinh) { this.soCauTrungBinh = soCauTrungBinh; }

    public long getSoCauKho() { return soCauKho; }
    public void setSoCauKho(long soCauKho) { this.soCauKho = soCauKho; }
}
