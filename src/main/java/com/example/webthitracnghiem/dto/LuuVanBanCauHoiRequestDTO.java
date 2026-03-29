package com.example.webthitracnghiem.dto;

import java.math.BigDecimal;

/**
 * PUT lưu văn bản thô câu hỏi trong đề.
 */
public class LuuVanBanCauHoiRequestDTO {

    private String vanBan;
    /** Tùy chọn: cập nhật thang điểm tối đa (chia đều cho từng câu). */
    private BigDecimal thangDiemToiDa;

    public String getVanBan() {
        return vanBan;
    }

    public void setVanBan(String vanBan) {
        this.vanBan = vanBan;
    }

    public BigDecimal getThangDiemToiDa() {
        return thangDiemToiDa;
    }

    public void setThangDiemToiDa(BigDecimal thangDiemToiDa) {
        this.thangDiemToiDa = thangDiemToiDa;
    }
}
