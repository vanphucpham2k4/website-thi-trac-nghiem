package com.example.webthitracnghiem.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Chi tiết lớp để sửa (tên + danh sách id sinh viên đã chọn).
 */
public class GiaoVienLopHocChiTietDTO {

    private String id;
    private String tenLop;
    private List<String> sinhVienIds = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public List<String> getSinhVienIds() {
        return sinhVienIds;
    }

    public void setSinhVienIds(List<String> sinhVienIds) {
        this.sinhVienIds = sinhVienIds != null ? sinhVienIds : new ArrayList<>();
    }
}
