package com.example.webthitracnghiem.dto;

/**
 * DTO — Một hàng kết quả sinh viên trong Bước 3 "Xem kết quả chi tiết".
 * 9 cột: STT, MSSV, Họ, Tên, Link truy cập, Mã code đã dùng, Điểm, Thời gian nộp, Ghi chú.
 */
public class GiaoVienKetQuaSinhVienItemDTO {

    private int stt;
    private String mssv;
    private String ho;
    private String ten;
    private String duongDanTruyCap;
    private String maTruyCapDaDung;
    private String diem;
    private String thoiGianNop;
    private String ghiChu;
    /** ID kết quả thi — để cập nhật ghi chú */
    private String ketQuaThiId;

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getMssv() { return mssv; }
    public void setMssv(String mssv) { this.mssv = mssv; }

    public String getHo() { return ho; }
    public void setHo(String ho) { this.ho = ho; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getDuongDanTruyCap() { return duongDanTruyCap; }
    public void setDuongDanTruyCap(String duongDanTruyCap) { this.duongDanTruyCap = duongDanTruyCap; }

    public String getMaTruyCapDaDung() { return maTruyCapDaDung; }
    public void setMaTruyCapDaDung(String maTruyCapDaDung) { this.maTruyCapDaDung = maTruyCapDaDung; }

    public String getDiem() { return diem; }
    public void setDiem(String diem) { this.diem = diem; }

    public String getThoiGianNop() { return thoiGianNop; }
    public void setThoiGianNop(String thoiGianNop) { this.thoiGianNop = thoiGianNop; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getKetQuaThiId() { return ketQuaThiId; }
    public void setKetQuaThiId(String ketQuaThiId) { this.ketQuaThiId = ketQuaThiId; }

    /** Nguồn: tên lớp hoặc "Link công khai" */
    private String nguon;
    public String getNguon() { return nguon; }
    public void setNguon(String nguon) { this.nguon = nguon; }
}
