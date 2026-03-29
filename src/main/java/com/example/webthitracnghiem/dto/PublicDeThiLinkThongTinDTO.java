package com.example.webthitracnghiem.dto;

/**
 * Thông tin đề thi hiển thị công khai qua link (không cần đăng nhập để xem trang landing).
 */
public class PublicDeThiLinkThongTinDTO {

    private String tenDeThi;
    private String tenMonHoc;
    private Integer thoiGianPhut;
    private long soCauHoi;

    public String getTenDeThi() {
        return tenDeThi;
    }

    public void setTenDeThi(String tenDeThi) {
        this.tenDeThi = tenDeThi;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
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
}
