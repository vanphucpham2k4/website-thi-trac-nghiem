package com.example.webthitracnghiem.dto;

import java.time.LocalDateTime;

/**
 * DTO — Thông tin đề thi dùng trong danh sách (list view).
 * Bao gồm thông tin cơ bản + thống kê lượt thi và số câu hỏi.
 */
public class DeThiListItemDTO {

    private String id;
    private String maDeThi;
    private String tenDeThi;
    private String monHocId;
    private String tenMonHoc;
    private Integer thoiGianPhut;
    private String moTa;

    /** NHAP = Nháp, CONG_KHAI = Công khai */
    private String trangThai;

    private LocalDateTime thoiGianMo;
    private LocalDateTime thoiGianDong;
    private Integer soLanThiToiDa;
    private Boolean tronCauHoi;
    private Boolean tronDapAn;

    /** Cho phép sinh viên xem lại chi tiết từng câu sau khi nộp bài. */
    private Boolean choPhepXemLai;

    /** Số lượt thi (từ bảng phien_thi) */
    private long soLuotThi;

    /** Số câu hỏi trong đề (từ bảng de_thi_cau_hoi) */
    private long soCauHoi;

    /** true nếu đề thi đã bị xóa mềm */
    private boolean daBiXoa;

    /** true nếu đề đã được xuất bản cho ít nhất 1 lớp */
    private boolean daXuatBan;

    /** true nếu đã có link tham gia công khai (mã truy cập) */
    private boolean coLinkThamGia;

    private LocalDateTime deletedAt;
    private LocalDateTime thoiGianTao;

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

    public boolean isDaBiXoa() { return daBiXoa; }
    public void setDaBiXoa(boolean daBiXoa) { this.daBiXoa = daBiXoa; }

    public boolean isDaXuatBan() { return daXuatBan; }
    public void setDaXuatBan(boolean daXuatBan) { this.daXuatBan = daXuatBan; }

    public boolean isCoLinkThamGia() { return coLinkThamGia; }
    public void setCoLinkThamGia(boolean coLinkThamGia) { this.coLinkThamGia = coLinkThamGia; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }
}
