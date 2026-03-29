package com.example.webthitracnghiem.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lưu đáp án: key = ID câu hỏi, value = A/B/C/D
 */
public class SinhVienLuuTraLoiDTO {

    private Map<String, String> traLoi = new LinkedHashMap<>();

    public Map<String, String> getTraLoi() {
        return traLoi;
    }

    public void setTraLoi(Map<String, String> traLoi) {
        this.traLoi = traLoi != null ? traLoi : new LinkedHashMap<>();
    }
}
