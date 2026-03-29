package com.example.webthitracnghiem.dto;

/**
 * Phản hồi khi giáo viên tạo / xem link tham gia đề công khai.
 */
public class DeThiLinkThamGiaResponseDTO {

    /** Mã token trong URL (vd: 32 ký tự hex) */
    private String maTruyCap;
    /** Đường dẫn tương đối, vd /thi-mo/abc... */
    private String duongDanTuongDoi;

    public String getMaTruyCap() {
        return maTruyCap;
    }

    public void setMaTruyCap(String maTruyCap) {
        this.maTruyCap = maTruyCap;
    }

    public String getDuongDanTuongDoi() {
        return duongDanTuongDoi;
    }

    public void setDuongDanTuongDoi(String duongDanTuongDoi) {
        this.duongDanTuongDoi = duongDanTuongDoi;
    }
}
