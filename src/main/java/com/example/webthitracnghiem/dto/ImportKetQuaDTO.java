package com.example.webthitracnghiem.dto;

import java.util.List;

/**
 * DTO — Kết quả phân tích file PDF/DOCX import đề thi.
 * Trả về danh sách câu hỏi đã parse được từ file.
 * Frontend có thể review lại trước khi lưu vào ngân hàng câu hỏi.
 */
public class ImportKetQuaDTO {

    private boolean success;
    private String message;
    private int tongSoCauHoi;
    private List<CauHoiImportDTO> cauHoiList;

    /** DTO lồng — một câu hỏi đã parse từ file */
    public static class CauHoiImportDTO {
        private int stt;
        private String noiDung;
        private String luaChonA;
        private String luaChonB;
        private String luaChonC;
        private String luaChonD;
        /** Đáp án đúng phân tích được: "A", "B", "C", "D" — null nếu không nhận diện được */
        private String dapAnDung;

        public int getStt() { return stt; }
        public void setStt(int stt) { this.stt = stt; }

        public String getNoiDung() { return noiDung; }
        public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

        public String getLuaChonA() { return luaChonA; }
        public void setLuaChonA(String luaChonA) { this.luaChonA = luaChonA; }

        public String getLuaChonB() { return luaChonB; }
        public void setLuaChonB(String luaChonB) { this.luaChonB = luaChonB; }

        public String getLuaChonC() { return luaChonC; }
        public void setLuaChonC(String luaChonC) { this.luaChonC = luaChonC; }

        public String getLuaChonD() { return luaChonD; }
        public void setLuaChonD(String luaChonD) { this.luaChonD = luaChonD; }

        public String getDapAnDung() { return dapAnDung; }
        public void setDapAnDung(String dapAnDung) { this.dapAnDung = dapAnDung; }
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getTongSoCauHoi() { return tongSoCauHoi; }
    public void setTongSoCauHoi(int tongSoCauHoi) { this.tongSoCauHoi = tongSoCauHoi; }

    public List<CauHoiImportDTO> getCauHoiList() { return cauHoiList; }
    public void setCauHoiList(List<CauHoiImportDTO> cauHoiList) { this.cauHoiList = cauHoiList; }
}
