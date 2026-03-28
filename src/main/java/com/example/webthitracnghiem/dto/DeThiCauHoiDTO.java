package com.example.webthitracnghiem.dto;

/**
 * DTO — Thông tin một câu hỏi bên trong đề thi.
 * Gồm thứ tự trong đề + toàn bộ thông tin câu hỏi để hiển thị.
 */
public class DeThiCauHoiDTO {

    /** ID của bản ghi liên kết de_thi_cau_hoi */
    private String linkId;

    /** Thứ tự câu hỏi trong đề (1, 2, 3...) */
    private int thuTu;

    /** Thông tin câu hỏi */
    private String cauHoiId;
    private String noiDung;
    private String loaiCauHoi;
    private String doKho;
    private String dapAnDung;
    private String luaChonA;
    private String luaChonB;
    private String luaChonC;
    private String luaChonD;
    private String tenChuDe;
    private String tenMonHoc;

    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }

    public int getThuTu() { return thuTu; }
    public void setThuTu(int thuTu) { this.thuTu = thuTu; }

    public String getCauHoiId() { return cauHoiId; }
    public void setCauHoiId(String cauHoiId) { this.cauHoiId = cauHoiId; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getLoaiCauHoi() { return loaiCauHoi; }
    public void setLoaiCauHoi(String loaiCauHoi) { this.loaiCauHoi = loaiCauHoi; }

    public String getDoKho() { return doKho; }
    public void setDoKho(String doKho) { this.doKho = doKho; }

    public String getDapAnDung() { return dapAnDung; }
    public void setDapAnDung(String dapAnDung) { this.dapAnDung = dapAnDung; }

    public String getLuaChonA() { return luaChonA; }
    public void setLuaChonA(String luaChonA) { this.luaChonA = luaChonA; }

    public String getLuaChonB() { return luaChonB; }
    public void setLuaChonB(String luaChonB) { this.luaChonB = luaChonB; }

    public String getLuaChonC() { return luaChonC; }
    public void setLuaChonC(String luaChonC) { this.luaChonC = luaChonC; }

    public String getLuaChonD() { return luaChonD; }
    public void setLuaChonD(String luaChonD) { this.luaChonD = luaChonD; }

    public String getTenChuDe() { return tenChuDe; }
    public void setTenChuDe(String tenChuDe) { this.tenChuDe = tenChuDe; }

    public String getTenMonHoc() { return tenMonHoc; }
    public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }
}
