package com.example.webthitracnghiem.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO — Yêu cầu thêm nhiều câu hỏi vào đề thi cùng lúc.
 */
public class ThemCauHoiVaoDeDTO {

    @NotEmpty(message = "Danh sách câu hỏi không được để trống")
    private List<String> cauHoiIds;

    public List<String> getCauHoiIds() { return cauHoiIds; }
    public void setCauHoiIds(List<String> cauHoiIds) { this.cauHoiIds = cauHoiIds; }
}
