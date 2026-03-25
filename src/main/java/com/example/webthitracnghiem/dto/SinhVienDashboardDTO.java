package com.example.webthitracnghiem.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO - Thống kê dashboard dành cho SINH VIÊN
 * Chứa các số liệu tổng quan về quá trình học tập của sinh viên
 */
public class SinhVienDashboardDTO {

    // ===== THÔNG TIN CÁ NHÂN =====
    /** ID người dùng */
    private String id;
    /** Mã sinh viên */
    private String maNguoiDung;
    /** Họ và tên đầy đủ */
    private String hoTen;
    /** Email */
    private String email;
    /** Số điện thoại */
    private String soDienThoai;

    // ===== THỐNG KÊ TỔNG QUAN =====
    /** Tổng số lần đã thi */
    private long tongSoLanThi;

    /** Số bài thi đã hoàn thành (có kết quả) */
    private long soBaiThiHoanThanh;

    /** Điểm trung bình tất cả các bài thi */
    private BigDecimal diemTrungBinh;

    /** Xếp hạng (nếu có) */
    private Integer xepHang;

    /** Tổng số môn học đã tham gia */
    private long soMonHocThamGia;

    // ===== ĐIỂM SỐ THEO TỪNG MÔN =====
    /** Danh sách điểm theo môn học */
    private List<MonDiemDTO> diemTheoMon;

    // ===== BÀI THI GẦN NHẤT =====
    /** Thông tin bài thi gần nhất */
    private KetQuaGanNhatDTO baiThiGanNhat;

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

    public long getTongSoLanThi() { return tongSoLanThi; }
    public void setTongSoLanThi(long tongSoLanThi) { this.tongSoLanThi = tongSoLanThi; }

    public long getSoBaiThiHoanThanh() { return soBaiThiHoanThanh; }
    public void setSoBaiThiHoanThanh(long soBaiThiHoanThanh) { this.soBaiThiHoanThanh = soBaiThiHoanThanh; }

    public BigDecimal getDiemTrungBinh() { return diemTrungBinh; }
    public void setDiemTrungBinh(BigDecimal diemTrungBinh) { this.diemTrungBinh = diemTrungBinh; }

    public Integer getXepHang() { return xepHang; }
    public void setXepHang(Integer xepHang) { this.xepHang = xepHang; }

    public long getSoMonHocThamGia() { return soMonHocThamGia; }
    public void setSoMonHocThamGia(long soMonHocThamGia) { this.soMonHocThamGia = soMonHocThamGia; }

    public List<MonDiemDTO> getDiemTheoMon() { return diemTheoMon; }
    public void setDiemTheoMon(List<MonDiemDTO> diemTheoMon) { this.diemTheoMon = diemTheoMon; }

    public KetQuaGanNhatDTO getBaiThiGanNhat() { return baiThiGanNhat; }
    public void setBaiThiGanNhat(KetQuaGanNhatDTO baiThiGanNhat) { this.baiThiGanNhat = baiThiGanNhat; }

    /**
     * Inner DTO - Điểm số theo từng môn học
     */
    public static class MonDiemDTO {
        /** Tên môn học */
        private String tenMon;
        /** Số lần thi */
        private int soLanThi;
        /** Điểm cao nhất */
        private BigDecimal diemCaoNhat;
        /** Điểm thấp nhất */
        private BigDecimal diemThapNhat;
        /** Điểm trung bình */
        private BigDecimal diemTrungBinhMon;

        // GETTER / SETTER
        public String getTenMon() { return tenMon; }
        public void setTenMon(String tenMon) { this.tenMon = tenMon; }

        public int getSoLanThi() { return soLanThi; }
        public void setSoLanThi(int soLanThi) { this.soLanThi = soLanThi; }

        public BigDecimal getDiemCaoNhat() { return diemCaoNhat; }
        public void setDiemCaoNhat(BigDecimal diemCaoNhat) { this.diemCaoNhat = diemCaoNhat; }

        public BigDecimal getDiemThapNhat() { return diemThapNhat; }
        public void setDiemThapNhat(BigDecimal diemThapNhat) { this.diemThapNhat = diemThapNhat; }

        public BigDecimal getDiemTrungBinhMon() { return diemTrungBinhMon; }
        public void setDiemTrungBinhMon(BigDecimal diemTrungBinhMon) { this.diemTrungBinhMon = diemTrungBinhMon; }
    }

    /**
     * Inner DTO - Kết quả bài thi gần nhất
     */
    public static class KetQuaGanNhatDTO {
        /** Tên đề thi */
        private String tenDeThi;
        /** Tên môn học */
        private String tenMonHoc;
        /** Điểm số */
        private BigDecimal diem;
        /** Ngày thi */
        private String ngayThi;
        /** Trạng thái chấm */
        private String trangThaiCham;

        // GETTER / SETTER
        public String getTenDeThi() { return tenDeThi; }
        public void setTenDeThi(String tenDeThi) { this.tenDeThi = tenDeThi; }

        public String getTenMonHoc() { return tenMonHoc; }
        public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }

        public BigDecimal getDiem() { return diem; }
        public void setDiem(BigDecimal diem) { this.diem = diem; }

        public String getNgayThi() { return ngayThi; }
        public void setNgayThi(String ngayThi) { this.ngayThi = ngayThi; }

        public String getTrangThaiCham() { return trangThaiCham; }
        public void setTrangThaiCham(String trangThaiCham) { this.trangThaiCham = trangThaiCham; }
    }
}
