package com.example.webthitracnghiem.dto;

/**
 * Admin cập nhật trạng thái / ghi chú yêu cầu đổi thưởng.
 */
public class AdminCapNhatYeuCauDoiThuongDTO {

    /** CHO_DUYET | DA_DUYET | DA_NHAN_QUA | DA_HUY */
    private String trangThai;
    private String ghiChu;

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
