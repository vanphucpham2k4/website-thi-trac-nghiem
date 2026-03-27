package com.example.webthitracnghiem.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO — Dữ liệu đầu vào khi tạo câu hỏi mới trong ngân hàng câu hỏi.
 * Hỗ trợ câu hỏi trắc nghiệm (TRAC_NGHIEM), đúng/sai (DUNG_SAI), tự luận (TU_LUAN).
 */
public class TaoCauHoiDTO {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String noiDung;

    @NotBlank(message = "Vui lòng chọn chủ đề")
    private String chuDeId;

    @NotBlank(message = "Vui lòng chọn loại câu hỏi")
    private String loaiCauHoi;

    @NotBlank(message = "Vui lòng chọn độ khó")
    private String doKho;

    @NotBlank(message = "Đáp án đúng không được để trống")
    private String dapAnDung;

    /** Các lựa chọn cho câu hỏi trắc nghiệm 4 đáp án */
    private String luaChonA;
    private String luaChonB;
    private String luaChonC;
    private String luaChonD;

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getChuDeId() { return chuDeId; }
    public void setChuDeId(String chuDeId) { this.chuDeId = chuDeId; }

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
}
