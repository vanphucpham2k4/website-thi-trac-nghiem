package com.example.webthitracnghiem.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Một môn học trong danh sách admin.
 */
public class AdminMonHocItemDTO {

    private String id;
    private String ten;
    private String moTa;
    /** Tên các chủ đề thuộc môn (từ bảng chu_de), sắp xếp theo tên. */
    private List<String> danhSachTenChuDe = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public List<String> getDanhSachTenChuDe() {
        return danhSachTenChuDe;
    }

    public void setDanhSachTenChuDe(List<String> danhSachTenChuDe) {
        this.danhSachTenChuDe = danhSachTenChuDe != null ? danhSachTenChuDe : new ArrayList<>();
    }
}
