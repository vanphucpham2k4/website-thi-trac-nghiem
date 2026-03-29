package com.example.webthitracnghiem.dto;

import java.util.ArrayList;
import java.util.List;

public class SinhVienBaiThiDTO {

    private String phienThiId;
    private String deThiId;
    private String tenDeThi;
    private String tenMonHoc;
    private String maHocPhan;
    private Integer thoiGianPhut;
    private String thoiGianBatDau;
    private String thoiGianHetHan;
    private List<SinhVienCauHoiThiDTO> cauHoi = new ArrayList<>();

    public String getPhienThiId() {
        return phienThiId;
    }

    public void setPhienThiId(String phienThiId) {
        this.phienThiId = phienThiId;
    }

    public String getDeThiId() {
        return deThiId;
    }

    public void setDeThiId(String deThiId) {
        this.deThiId = deThiId;
    }

    public String getTenDeThi() {
        return tenDeThi;
    }

    public void setTenDeThi(String tenDeThi) {
        this.tenDeThi = tenDeThi;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public String getMaHocPhan() {
        return maHocPhan;
    }

    public void setMaHocPhan(String maHocPhan) {
        this.maHocPhan = maHocPhan;
    }

    public Integer getThoiGianPhut() {
        return thoiGianPhut;
    }

    public void setThoiGianPhut(Integer thoiGianPhut) {
        this.thoiGianPhut = thoiGianPhut;
    }

    public String getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(String thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public String getThoiGianHetHan() {
        return thoiGianHetHan;
    }

    public void setThoiGianHetHan(String thoiGianHetHan) {
        this.thoiGianHetHan = thoiGianHetHan;
    }

    public List<SinhVienCauHoiThiDTO> getCauHoi() {
        return cauHoi;
    }

    public void setCauHoi(List<SinhVienCauHoiThiDTO> cauHoi) {
        this.cauHoi = cauHoi != null ? cauHoi : new ArrayList<>();
    }
}
