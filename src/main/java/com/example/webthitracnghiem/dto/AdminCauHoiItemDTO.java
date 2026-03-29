package com.example.webthitracnghiem.dto;

/**
 * DTO — Thông tin câu hỏi dùng cho Admin (cấp 2, bao gồm email người tạo).
 * Kế thừa tất cả trường từ CauHoiListItemDTO + emailNguoiTao.
 */
public class AdminCauHoiItemDTO {

    private String id;
    private String noiDung;
    private String loaiCauHoi;
    private String doKho;
    private String dapAnDung;
    private String luaChonA;
    private String luaChonB;
    private String luaChonC;
    private String luaChonD;
    private String chuDeId;
    private String tenChuDe;
    private String monHocId;
    private String tenMonHoc;
    private String nguoiTaoId;
    private String tenNguoiTao;
    private String emailNguoiTao;
    private long soDeThiSuDung;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getChuDeId() { return chuDeId; }
    public void setChuDeId(String chuDeId) { this.chuDeId = chuDeId; }

    public String getTenChuDe() { return tenChuDe; }
    public void setTenChuDe(String tenChuDe) { this.tenChuDe = tenChuDe; }

    public String getMonHocId() { return monHocId; }
    public void setMonHocId(String monHocId) { this.monHocId = monHocId; }

    public String getTenMonHoc() { return tenMonHoc; }
    public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }

    public String getNguoiTaoId() { return nguoiTaoId; }
    public void setNguoiTaoId(String nguoiTaoId) { this.nguoiTaoId = nguoiTaoId; }

    public String getTenNguoiTao() { return tenNguoiTao; }
    public void setTenNguoiTao(String tenNguoiTao) { this.tenNguoiTao = tenNguoiTao; }

    public String getEmailNguoiTao() { return emailNguoiTao; }
    public void setEmailNguoiTao(String emailNguoiTao) { this.emailNguoiTao = emailNguoiTao; }

    public long getSoDeThiSuDung() { return soDeThiSuDung; }
    public void setSoDeThiSuDung(long soDeThiSuDung) { this.soDeThiSuDung = soDeThiSuDung; }
}
