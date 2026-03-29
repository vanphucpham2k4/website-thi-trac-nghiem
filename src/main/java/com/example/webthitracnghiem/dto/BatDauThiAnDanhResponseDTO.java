package com.example.webthitracnghiem.dto;

/**
 * Trả về phiên thi + JWT làm bài (ẩn danh).
 */
public class BatDauThiAnDanhResponseDTO {

    private String phienThiId;
    private String lamBaiToken;
    private long expiresAt;

    public String getPhienThiId() {
        return phienThiId;
    }

    public void setPhienThiId(String phienThiId) {
        this.phienThiId = phienThiId;
    }

    public String getLamBaiToken() {
        return lamBaiToken;
    }

    public void setLamBaiToken(String lamBaiToken) {
        this.lamBaiToken = lamBaiToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
