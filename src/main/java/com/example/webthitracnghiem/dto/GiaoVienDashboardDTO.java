package com.example.webthitracnghiem.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO - Thống kê dashboard dành cho GIÁO VIÊN
 * Chứa các số liệu tổng quan về hoạt động quản lý đề thi của giáo viên
 */
public class GiaoVienDashboardDTO {

    // ===== THÔNG TIN CÁ NHÂN =====
    /** ID người dùng */
    private String id;
    /** Mã giáo viên */
    private String maNguoiDung;
    /** Họ và tên đầy đủ */
    private String hoTen;
    /** Email */
    private String email;
    /** Số điện thoại */
    private String soDienThoai;

    // ===== THỐNG KÊ TỔNG QUAN =====
    /** Tổng số đề thi đã tạo */
    private long tongSoDeThi;

    /** Tổng số câu hỏi đã tạo */
    private long tongSoCauHoi;

    /** Tổng số lượt thi của tất cả sinh viên */
    private long tongSoLuotThi;

    /** Tổng số sinh viên đã tham gia thi */
    private long tongSoSinhVien;

    /** Điểm trung bình chung của tất cả các bài thi */
    private BigDecimal diemTrungBinhChung;

    // ===== ĐỀ THI GẦN NHẤT =====
    /** Thông tin đề thi được tạo gần nhất */
    private DeThiGanNhatDTO deThiGanNhat;

    // ===== DANH SÁCH ĐỀ THI GẦN ĐÂY =====
    /** Danh sách các đề thi gần đây */
    private List<DeThiDTO> deThiGanDay;

    // ===== THỐNG KÊ THEO MÔN HỌC =====
    /** Danh sách thống kê theo môn học */
    private List<MonHocThongKeDTO> thongKeTheoMon;

    // ===== GETTER / SETTER =====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(String maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public long getTongSoDeThi() { return tongSoDeThi; }
    public void setTongSoDeThi(long tongSoDeThi) { this.tongSoDeThi = tongSoDeThi; }

    public long getTongSoCauHoi() { return tongSoCauHoi; }
    public void setTongSoCauHoi(long tongSoCauHoi) { this.tongSoCauHoi = tongSoCauHoi; }

    public long getTongSoLuotThi() { return tongSoLuotThi; }
    public void setTongSoLuotThi(long tongSoLuotThi) { this.tongSoLuotThi = tongSoLuotThi; }

    public long getTongSoSinhVien() { return tongSoSinhVien; }
    public void setTongSoSinhVien(long tongSoSinhVien) { this.tongSoSinhVien = tongSoSinhVien; }

    public BigDecimal getDiemTrungBinhChung() { return diemTrungBinhChung; }
    public void setDiemTrungBinhChung(BigDecimal diemTrungBinhChung) { this.diemTrungBinhChung = diemTrungBinhChung; }

    public DeThiGanNhatDTO getDeThiGanNhat() { return deThiGanNhat; }
    public void setDeThiGanNhat(DeThiGanNhatDTO deThiGanNhat) { this.deThiGanNhat = deThiGanNhat; }

    public List<DeThiDTO> getDeThiGanDay() { return deThiGanDay; }
    public void setDeThiGanDay(List<DeThiDTO> deThiGanDay) { this.deThiGanDay = deThiGanDay; }

    public List<MonHocThongKeDTO> getThongKeTheoMon() { return thongKeTheoMon; }
    public void setThongKeTheoMon(List<MonHocThongKeDTO> thongKeTheoMon) { this.thongKeTheoMon = thongKeTheoMon; }

    /**
     * Inner DTO - Thông tin đề thi gần nhất
     */
    public static class DeThiGanNhatDTO {
        /** ID đề thi */
        private String id;
        /** Tên đề thi */
        private String tenDeThi;
        /** Tên môn học */
        private String tenMonHoc;
        /** Thời gian thi (phút) */
        private Integer thoiGianPhut;
        /** Số lượt thi */
        private long soLuotThi;
        /** Điểm trung bình */
        private BigDecimal diemTrungBinh;
        /** Trạng thái (Đã mở / Đã đóng) */
        private String trangThai;

        // GETTER / SETTER
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTenDeThi() { return tenDeThi; }
        public void setTenDeThi(String tenDeThi) { this.tenDeThi = tenDeThi; }

        public String getTenMonHoc() { return tenMonHoc; }
        public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }

        public Integer getThoiGianPhut() { return thoiGianPhut; }
        public void setThoiGianPhut(Integer thoiGianPhut) { this.thoiGianPhut = thoiGianPhut; }

        public long getSoLuotThi() { return soLuotThi; }
        public void setSoLuotThi(long soLuotThi) { this.soLuotThi = soLuotThi; }

        public BigDecimal getDiemTrungBinh() { return diemTrungBinh; }
        public void setDiemTrungBinh(BigDecimal diemTrungBinh) { this.diemTrungBinh = diemTrungBinh; }

        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }

    /**
     * Inner DTO - Thông tin một đề thi
     */
    public static class DeThiDTO {
        /** ID đề thi */
        private String id;
        /** Mã đề thi */
        private String maDeThi;
        /** Tên đề thi */
        private String tenDeThi;
        /** Tên môn học */
        private String tenMonHoc;
        /** Thời gian thi (phút) */
        private Integer thoiGianPhut;
        /** Mô tả */
        private String moTa;
        /** Số câu hỏi */
        private int soCauHoi;
        /** Số lượt thi */
        private long soLuotThi;
        /** Trạng thái */
        private String trangThai;
        /** Thời gian tạo */
        private String thoiGianTao;

        // GETTER / SETTER
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getMaDeThi() { return maDeThi; }
        public void setMaDeThi(String maDeThi) { this.maDeThi = maDeThi; }

        public String getTenDeThi() { return tenDeThi; }
        public void setTenDeThi(String tenDeThi) { this.tenDeThi = tenDeThi; }

        public String getTenMonHoc() { return tenMonHoc; }
        public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }

        public Integer getThoiGianPhut() { return thoiGianPhut; }
        public void setThoiGianPhut(Integer thoiGianPhut) { this.thoiGianPhut = thoiGianPhut; }

        public String getMoTa() { return moTa; }
        public void setMoTa(String moTa) { this.moTa = moTa; }

        public int getSoCauHoi() { return soCauHoi; }
        public void setSoCauHoi(int soCauHoi) { this.soCauHoi = soCauHoi; }

        public long getSoLuotThi() { return soLuotThi; }
        public void setSoLuotThi(long soLuotThi) { this.soLuotThi = soLuotThi; }

        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

        public String getThoiGianTao() { return thoiGianTao; }
        public void setThoiGianTao(String thoiGianTao) { this.thoiGianTao = thoiGianTao; }
    }

    /**
     * Inner DTO - Thống kê theo môn học
     */
    public static class MonHocThongKeDTO {
        /** Tên môn học */
        private String tenMonHoc;
        /** Số đề thi */
        private long soDeThi;
        /** Số câu hỏi */
        private long soCauHoi;
        /** Số lượt thi */
        private long soLuotThi;
        /** Điểm trung bình */
        private BigDecimal diemTrungBinh;

        // GETTER / SETTER
        public String getTenMonHoc() { return tenMonHoc; }
        public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }

        public long getSoDeThi() { return soDeThi; }
        public void setSoDeThi(long soDeThi) { this.soDeThi = soDeThi; }

        public long getSoCauHoi() { return soCauHoi; }
        public void setSoCauHoi(long soCauHoi) { this.soCauHoi = soCauHoi; }

        public long getSoLuotThi() { return soLuotThi; }
        public void setSoLuotThi(long soLuotThi) { this.soLuotThi = soLuotThi; }

        public BigDecimal getDiemTrungBinh() { return diemTrungBinh; }
        public void setDiemTrungBinh(BigDecimal diemTrungBinh) { this.diemTrungBinh = diemTrungBinh; }
    }
}
