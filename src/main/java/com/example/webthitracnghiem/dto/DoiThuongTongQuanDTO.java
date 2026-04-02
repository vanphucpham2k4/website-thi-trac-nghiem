package com.example.webthitracnghiem.dto;

/**
 * Thống kê đầu trang Đổi thưởng.
 */
public class DoiThuongTongQuanDTO {

    /** Điểm khả dụng = tổng điểm bài thi (làm tròn) − điểm đã dùng đổi quà (trừ yêu cầu đã hủy). */
    private int diemHienTai;
    /** Số lượt đổi đã ghi nhận (chờ duyệt / đã duyệt / đã nhận quà; không tính yêu cầu đã hủy). */
    private long tongLuotDoiThanhCong;
    /** Tổng điểm tích lũy từ kết quả thi (trước khi trừ đổi). */
    private int diemTichLuyTuBaiThi;
    /** Tổng điểm đã cam kết cho các yêu cầu chưa hủy. */
    private int diemDaSuDung;

    public int getDiemHienTai() {
        return diemHienTai;
    }

    public void setDiemHienTai(int diemHienTai) {
        this.diemHienTai = diemHienTai;
    }

    public long getTongLuotDoiThanhCong() {
        return tongLuotDoiThanhCong;
    }

    public void setTongLuotDoiThanhCong(long tongLuotDoiThanhCong) {
        this.tongLuotDoiThanhCong = tongLuotDoiThanhCong;
    }

    public int getDiemTichLuyTuBaiThi() {
        return diemTichLuyTuBaiThi;
    }

    public void setDiemTichLuyTuBaiThi(int diemTichLuyTuBaiThi) {
        this.diemTichLuyTuBaiThi = diemTichLuyTuBaiThi;
    }

    public int getDiemDaSuDung() {
        return diemDaSuDung;
    }

    public void setDiemDaSuDung(int diemDaSuDung) {
        this.diemDaSuDung = diemDaSuDung;
    }
}
