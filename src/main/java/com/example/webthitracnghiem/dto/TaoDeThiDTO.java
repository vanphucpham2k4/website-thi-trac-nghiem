package com.example.webthitracnghiem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO — Dữ liệu đầu vào khi tạo đề thi mới.
 * Áp dụng Bean Validation để kiểm tra tính hợp lệ trước khi xử lý.
 */
public class TaoDeThiDTO {

    @NotBlank(message = "Tên đề thi không được để trống")
    private String tenDeThi;

    @NotBlank(message = "Vui lòng chọn môn học")
    private String monHocId;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Min(value = 1, message = "Thời gian làm bài phải ít nhất 1 phút")
    private Integer thoiGianPhut;

    private String moTa;

    /** Trạng thái ban đầu: NHAP hoặc CONG_KHAI. Mặc định NHAP nếu không truyền. */
    private String trangThai = "NHAP";

    private LocalDateTime thoiGianMo;
    private LocalDateTime thoiGianDong;

    /** Số lần thi tối đa cho mỗi sinh viên. Null = không giới hạn. */
    @Min(value = 1, message = "Số lần thi tối đa phải ít nhất 1")
    private Integer soLanThiToiDa;

    /** Null = mặc định cho phép xem lại chi tiết sau thi. */
    private Boolean choPhepXemLai;

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

    public Boolean getChoPhepXemLai() { return choPhepXemLai; }
    public void setChoPhepXemLai(Boolean choPhepXemLai) { this.choPhepXemLai = choPhepXemLai; }
}
