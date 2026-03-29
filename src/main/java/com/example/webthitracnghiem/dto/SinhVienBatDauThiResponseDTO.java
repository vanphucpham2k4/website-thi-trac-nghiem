package com.example.webthitracnghiem.dto;

public class SinhVienBatDauThiResponseDTO {

    private String phienThiId;
    private boolean tiepTucPhienCu;

    public String getPhienThiId() {
        return phienThiId;
    }

    public void setPhienThiId(String phienThiId) {
        this.phienThiId = phienThiId;
    }

    public boolean isTiepTucPhienCu() {
        return tiepTucPhienCu;
    }

    public void setTiepTucPhienCu(boolean tiepTucPhienCu) {
        this.tiepTucPhienCu = tiepTucPhienCu;
    }
}
