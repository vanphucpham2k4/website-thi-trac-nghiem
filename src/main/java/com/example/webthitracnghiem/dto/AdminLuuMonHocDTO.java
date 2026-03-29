package com.example.webthitracnghiem.dto;

/**
 * Dữ liệu tạo / cập nhật môn học (admin).
 * Chủ đề: mỗi dòng trong {@link #tenChuDeTheoDong} là một tên chủ đề (thứ tự khớp bản ghi theo id tăng dần).
 */
public class AdminLuuMonHocDTO {

    private String ten;
    private String moTa;
    /** Mỗi dòng một tên chủ đề; có thể rỗng. */
    private String tenChuDeTheoDong;

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getTenChuDeTheoDong() {
        return tenChuDeTheoDong;
    }

    public void setTenChuDeTheoDong(String tenChuDeTheoDong) {
        this.tenChuDeTheoDong = tenChuDeTheoDong;
    }
}
