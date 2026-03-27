package com.example.webthitracnghiem.dto;

import java.util.ArrayList;
import java.util.List;

public class TheoDoiSinhVienThiDTO {

    private String deThiId;
    private String tenDeThi;

    private long tongSo;
    private long soChuaVaoThi;
    private long soDangThi;
    private long soDaNopBai;
    private long soDaVaoChuaNop;

    private List<TheoDoiSinhVienThiItemDTO> danhSach = new ArrayList<>();

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

    public long getTongSo() {
        return tongSo;
    }

    public void setTongSo(long tongSo) {
        this.tongSo = tongSo;
    }

    public long getSoChuaVaoThi() {
        return soChuaVaoThi;
    }

    public void setSoChuaVaoThi(long soChuaVaoThi) {
        this.soChuaVaoThi = soChuaVaoThi;
    }

    public long getSoDangThi() {
        return soDangThi;
    }

    public void setSoDangThi(long soDangThi) {
        this.soDangThi = soDangThi;
    }

    public long getSoDaNopBai() {
        return soDaNopBai;
    }

    public void setSoDaNopBai(long soDaNopBai) {
        this.soDaNopBai = soDaNopBai;
    }

    public long getSoDaVaoChuaNop() {
        return soDaVaoChuaNop;
    }

    public void setSoDaVaoChuaNop(long soDaVaoChuaNop) {
        this.soDaVaoChuaNop = soDaVaoChuaNop;
    }

    public List<TheoDoiSinhVienThiItemDTO> getDanhSach() {
        return danhSach;
    }

    public void setDanhSach(List<TheoDoiSinhVienThiItemDTO> danhSach) {
        this.danhSach = danhSach;
    }
}
