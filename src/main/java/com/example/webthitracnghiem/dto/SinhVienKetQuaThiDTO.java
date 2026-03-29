package com.example.webthitracnghiem.dto;

import java.util.ArrayList;
import java.util.List;

public class SinhVienKetQuaThiDTO {

    private String phienThiId;
    private String tenDeThi;
    private String ngayThi;
    private String maHocPhan;
    private String tenGiaoVien;
    private String diemDat;
    private String diemToiDa;
    private String thoiGianLamBai;
    private int thoiGianGioiHanPhut;
    private int soCauDung;
    private int tongSoCau;
    private List<SinhVienOTrangThaiCauDTO> trangThaiCacCau = new ArrayList<>();

    /** true khi đề cho phép xem lại chi tiết từng câu (API + UI). */
    private boolean duocXemChiTiet = true;

    public String getPhienThiId() {
        return phienThiId;
    }

    public void setPhienThiId(String phienThiId) {
        this.phienThiId = phienThiId;
    }

    public String getTenDeThi() {
        return tenDeThi;
    }

    public void setTenDeThi(String tenDeThi) {
        this.tenDeThi = tenDeThi;
    }

    public String getNgayThi() {
        return ngayThi;
    }

    public void setNgayThi(String ngayThi) {
        this.ngayThi = ngayThi;
    }

    public String getMaHocPhan() {
        return maHocPhan;
    }

    public void setMaHocPhan(String maHocPhan) {
        this.maHocPhan = maHocPhan;
    }

    public String getTenGiaoVien() {
        return tenGiaoVien;
    }

    public void setTenGiaoVien(String tenGiaoVien) {
        this.tenGiaoVien = tenGiaoVien;
    }

    public String getDiemDat() {
        return diemDat;
    }

    public void setDiemDat(String diemDat) {
        this.diemDat = diemDat;
    }

    public String getDiemToiDa() {
        return diemToiDa;
    }

    public void setDiemToiDa(String diemToiDa) {
        this.diemToiDa = diemToiDa;
    }

    public String getThoiGianLamBai() {
        return thoiGianLamBai;
    }

    public void setThoiGianLamBai(String thoiGianLamBai) {
        this.thoiGianLamBai = thoiGianLamBai;
    }

    public int getThoiGianGioiHanPhut() {
        return thoiGianGioiHanPhut;
    }

    public void setThoiGianGioiHanPhut(int thoiGianGioiHanPhut) {
        this.thoiGianGioiHanPhut = thoiGianGioiHanPhut;
    }

    public int getSoCauDung() {
        return soCauDung;
    }

    public void setSoCauDung(int soCauDung) {
        this.soCauDung = soCauDung;
    }

    public int getTongSoCau() {
        return tongSoCau;
    }

    public void setTongSoCau(int tongSoCau) {
        this.tongSoCau = tongSoCau;
    }

    public List<SinhVienOTrangThaiCauDTO> getTrangThaiCacCau() {
        return trangThaiCacCau;
    }

    public void setTrangThaiCacCau(List<SinhVienOTrangThaiCauDTO> trangThaiCacCau) {
        this.trangThaiCacCau = trangThaiCacCau != null ? trangThaiCacCau : new ArrayList<>();
    }

    public boolean isDuocXemChiTiet() {
        return duocXemChiTiet;
    }

    public void setDuocXemChiTiet(boolean duocXemChiTiet) {
        this.duocXemChiTiet = duocXemChiTiet;
    }
}
