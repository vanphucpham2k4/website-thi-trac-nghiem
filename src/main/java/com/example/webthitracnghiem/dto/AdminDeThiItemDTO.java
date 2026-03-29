package com.example.webthitracnghiem.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO — Thông tin đề thi dùng cho Admin (cấp 2, bao gồm thông tin người tạo).
 */
public class AdminDeThiItemDTO {

    private String id;
    private String maDeThi;
    private String tenDeThi;
    private String monHocId;
    private String tenMonHoc;
    private Integer thoiGianPhut;
    private String moTa;
    private String trangThai;
    private LocalDateTime thoiGianMo;
    private LocalDateTime thoiGianDong;
    private Integer soLanThiToiDa;
    private Boolean tronCauHoi;
    private Boolean tronDapAn;
    private Boolean choPhepXemLai;
    private long soLuotThi;
    private long soCauHoi;
    private LocalDateTime thoiGianTao;

    // Thông tin người tạo
    private String nguoiTaoId;
    private String tenNguoiTao;

    // Chi tiết câu hỏi (chỉ populate khi xem chi tiết 1 đề)
    private List<CauHoiListItemDTO> danhSachCauHoi;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaDeThi() { return maDeThi; }
    public void setMaDeThi(String maDeThi) { this.maDeThi = maDeThi; }

    public String getTenDeThi() { return tenDeThi; }
    public void setTenDeThi(String tenDeThi) { this.tenDeThi = tenDeThi; }

    public String getMonHocId() { return monHocId; }
    public void setMonHocId(String monHocId) { this.monHocId = monHocId; }

    public String getTenMonHoc() { return tenMonHoc; }
    public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }

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

    public Boolean getChoPhepXemLai() { return choPhepXemLai; }
    public void setChoPhepXemLai(Boolean choPhepXemLai) { this.choPhepXemLai = choPhepXemLai; }

    public long getSoLuotThi() { return soLuotThi; }
    public void setSoLuotThi(long soLuotThi) { this.soLuotThi = soLuotThi; }

    public long getSoCauHoi() { return soCauHoi; }
    public void setSoCauHoi(long soCauHoi) { this.soCauHoi = soCauHoi; }

    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public String getNguoiTaoId() { return nguoiTaoId; }
    public void setNguoiTaoId(String nguoiTaoId) { this.nguoiTaoId = nguoiTaoId; }

    public String getTenNguoiTao() { return tenNguoiTao; }
    public void setTenNguoiTao(String tenNguoiTao) { this.tenNguoiTao = tenNguoiTao; }

    public List<CauHoiListItemDTO> getDanhSachCauHoi() { return danhSachCauHoi; }
    public void setDanhSachCauHoi(List<CauHoiListItemDTO> danhSachCauHoi) { this.danhSachCauHoi = danhSachCauHoi; }
}
