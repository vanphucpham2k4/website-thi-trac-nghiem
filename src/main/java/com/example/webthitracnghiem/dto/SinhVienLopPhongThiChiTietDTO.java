package com.example.webthitracnghiem.dto;

/**
 * Tiêu đề trang chi tiết lớp (sinh viên) — đề thi sẽ bổ sung sau.
 */
public class SinhVienLopPhongThiChiTietDTO {

    private String lopId;
    private String tenLop;
    private String tenChuTri;

    public String getLopId() {
        return lopId;
    }

    public void setLopId(String lopId) {
        this.lopId = lopId;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public String getTenChuTri() {
        return tenChuTri;
    }

    public void setTenChuTri(String tenChuTri) {
        this.tenChuTri = tenChuTri;
    }
}
