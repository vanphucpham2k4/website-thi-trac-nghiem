package com.example.webthitracnghiem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO — Dữ liệu đầu vào khi cập nhật đề thi.
 * Bao gồm đầy đủ các trường có thể chỉnh sửa: nội dung, thời gian, trạng thái, cài đặt.
 */
public class CapNhatDeThiDTO {

    @NotBlank(message = "Tên đề thi không được để trống")
    private String tenDeThi;

    @NotBlank(message = "Vui lòng chọn môn học")
    private String monHocId;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Min(value = 1, message = "Thời gian làm bài phải ít nhất 1 phút")
    private Integer thoiGianPhut;

    private String moTa;

    /** Trạng thái: NHAP (nháp) hoặc CONG_KHAI (công khai) */
    private String trangThai;

    private LocalDateTime thoiGianMo;
    private LocalDateTime thoiGianDong;

    @Min(value = 1, message = "Số lần thi tối đa phải ít nhất 1")
    private Integer soLanThiToiDa;

    private Boolean tronCauHoi;
    private Boolean tronDapAn;

    public String getTenDeThi() { return tenDeThi; }
    public void setTenDeThi(String tenDeThi) { this.tenDeThi = tenDeThi; }

    public String getMonHocId() { return monHocId; }
    public void setMonHocId(String monHocId) { this.monHocId = monHocId; }

    public Integer getThoiGianPhut() { return thoiGianPhut; }
    public void setThoiGianPhut(Integer thoiGianPhut) { this.thoiGianPhut = thoiGianPhut; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getThoiGianMo() { return thoiGianMo; }
    public void setThoiGianMo(LocalDateTime thoiGianMo) { this.thoiGianMo = thoiGianMo; }

    public LocalDateTime getThoiGianDong() { return thoiGianDong; }
    public void setThoiGianDong(LocalDateTime thoiGianDong) { this.thoiGianDong = thoiGianDong; }

    public Integer getSoLanThiToiDa() { return soLanThiToiDa; }
    public void setSoLanThiToiDa(Integer soLanThiToiDa) { this.soLanThiToiDa = soLanThiToiDa; }

    public Boolean getTronCauHoi() { return tronCauHoi; }
    public void setTronCauHoi(Boolean tronCauHoi) { this.tronCauHoi = tronCauHoi; }

    public Boolean getTronDapAn() { return tronDapAn; }
    public void setTronDapAn(Boolean tronDapAn) { this.tronDapAn = tronDapAn; }
}
