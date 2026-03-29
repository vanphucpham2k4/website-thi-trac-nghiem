package com.example.webthitracnghiem.dto;

public class SinhVienCauHoiThiDTO {

    private String id;
    private int thuTu;
    private String noiDung;
    private String loaiCauHoi;
    private String luaChonA;
    private String luaChonB;
    private String luaChonC;
    private String luaChonD;
    /** Đáp án SV đã chọn (A–D), có thể null */
    private String daChon;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getLoaiCauHoi() {
        return loaiCauHoi;
    }

    public void setLoaiCauHoi(String loaiCauHoi) {
        this.loaiCauHoi = loaiCauHoi;
    }

    public String getLuaChonA() {
        return luaChonA;
    }

    public void setLuaChonA(String luaChonA) {
        this.luaChonA = luaChonA;
    }

    public String getLuaChonB() {
        return luaChonB;
    }

    public void setLuaChonB(String luaChonB) {
        this.luaChonB = luaChonB;
    }

    public String getLuaChonC() {
        return luaChonC;
    }

    public void setLuaChonC(String luaChonC) {
        this.luaChonC = luaChonC;
    }

    public String getLuaChonD() {
        return luaChonD;
    }

    public void setLuaChonD(String luaChonD) {
        this.luaChonD = luaChonD;
    }

    public String getDaChon() {
        return daChon;
    }

    public void setDaChon(String daChon) {
        this.daChon = daChon;
    }
}
