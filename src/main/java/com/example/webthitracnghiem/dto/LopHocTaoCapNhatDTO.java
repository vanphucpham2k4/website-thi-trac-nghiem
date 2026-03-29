package com.example.webthitracnghiem.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Tạo / cập nhật lớp học (giáo viên).
 */
public class LopHocTaoCapNhatDTO {

    private String tenLop;
    private List<String> sinhVienIds = new ArrayList<>();

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
