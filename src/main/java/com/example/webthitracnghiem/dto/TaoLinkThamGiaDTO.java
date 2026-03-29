package com.example.webthitracnghiem.dto;

/**
 * Body tùy chọn: tạo mã link mới (vô hiệu hóa link cũ).
 */
public class TaoLinkThamGiaDTO {

    /** true = sinh lại mã, link cũ không còn hiệu lực */
    private boolean taoMoi;

    public boolean isTaoMoi() {
        return taoMoi;
    }

    public void setTaoMoi(boolean taoMoi) {
        this.taoMoi = taoMoi;
    }
}
