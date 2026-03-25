package com.example.webthitracnghiem.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO - Thống kê dashboard dành cho ADMIN (Quản trị viên)
 * Chứa toàn bộ số liệu hệ thống để admin theo dõi và quản lý
 */
public class AdminDashboardDTO {

    // ===== THÔNG TIN CÁ NHÂN ADMIN =====
    private String id;
    private String maNguoiDung;
    private String hoTen;
    private String email;
    private String soDienThoai;

    // ===== THỐNG KÊ TỔNG QUAN HỆ THỐNG =====
    /** Tổng số người dùng */
    private long tongSoNguoiDung;
    /** Tổng số sinh viên */
    private long tongSoSinhVien;
    /** Tổng số giáo viên */
    private long tongSoGiaoVien;
    /** Tổng số môn học */
    private long tongSoMonHoc;
    /** Tổng số đề thi */
    private long tongSoDeThi;
    /** Tổng số câu hỏi */
    private long tongSoCauHoi;
    /** Tổng số lượt thi */
    private long tongSoLuotThi;

    // ===== THỐNG KÊ ĐIỂM =====
    /** Điểm trung bình chung toàn hệ thống */
    private BigDecimal diemTrungBinhHeThong;
    /** Tỷ lệ đỗ (%) */
    private BigDecimal tiLeDo;

    // ===== BIỂU ĐỒ =====
    /** Dữ liệu thi theo tháng (12 tháng gần nhất) */
    private List<ThongKeThangDTO> thongKeTheoThang;
    /** Thống kê theo môn học */
    private List<MonHocThongKeDTO> thongKeTheoMon;
    /** Top giáo viên tích cực */
    private List<GiaoVienDTO> topGiaoVien;
    /** Top sinh viên xuất sắc */
    private List<SinhVienDTO> topSinhVien;

    // ===== HOẠT ĐỘNG GẦN ĐÂY =====
    /** Danh sách người dùng mới đăng ký gần đây */
    private List<NguoiDungDTO> nguoiDungMoi;
    /** Danh sách đề thi mới tạo gần đây */
    private List<DeThiMoiDTO> deThiMoi;

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

    public long getTongSoNguoiDung() { return tongSoNguoiDung; }
    public void setTongSoNguoiDung(long tongSoNguoiDung) { this.tongSoNguoiDung = tongSoNguoiDung; }

    public long getTongSoSinhVien() { return tongSoSinhVien; }
    public void setTongSoSinhVien(long tongSoSinhVien) { this.tongSoSinhVien = tongSoSinhVien; }

    public long getTongSoGiaoVien() { return tongSoGiaoVien; }
    public void setTongSoGiaoVien(long tongSoGiaoVien) { this.tongSoGiaoVien = tongSoGiaoVien; }

    public long getTongSoMonHoc() { return tongSoMonHoc; }
    public void setTongSoMonHoc(long tongSoMonHoc) { this.tongSoMonHoc = tongSoMonHoc; }

    public long getTongSoDeThi() { return tongSoDeThi; }
    public void setTongSoDeThi(long tongSoDeThi) { this.tongSoDeThi = tongSoDeThi; }

    public long getTongSoCauHoi() { return tongSoCauHoi; }
    public void setTongSoCauHoi(long tongSoCauHoi) { this.tongSoCauHoi = tongSoCauHoi; }

    public long getTongSoLuotThi() { return tongSoLuotThi; }
    public void setTongSoLuotThi(long tongSoLuotThi) { this.tongSoLuotThi = tongSoLuotThi; }

    public BigDecimal getDiemTrungBinhHeThong() { return diemTrungBinhHeThong; }
    public void setDiemTrungBinhHeThong(BigDecimal diemTrungBinhHeThong) { this.diemTrungBinhHeThong = diemTrungBinhHeThong; }

    public BigDecimal getTiLeDo() { return tiLeDo; }
    public void setTiLeDo(BigDecimal tiLeDo) { this.tiLeDo = tiLeDo; }

    public List<ThongKeThangDTO> getThongKeTheoThang() { return thongKeTheoThang; }
    public void setThongKeTheoThang(List<ThongKeThangDTO> thongKeTheoThang) { this.thongKeTheoThang = thongKeTheoThang; }

    public List<MonHocThongKeDTO> getThongKeTheoMon() { return thongKeTheoMon; }
    public void setThongKeTheoMon(List<MonHocThongKeDTO> thongKeTheoMon) { this.thongKeTheoMon = thongKeTheoMon; }

    public List<GiaoVienDTO> getTopGiaoVien() { return topGiaoVien; }
    public void setTopGiaoVien(List<GiaoVienDTO> topGiaoVien) { this.topGiaoVien = topGiaoVien; }

    public List<SinhVienDTO> getTopSinhVien() { return topSinhVien; }
    public void setTopSinhVien(List<SinhVienDTO> topSinhVien) { this.topSinhVien = topSinhVien; }

    public List<NguoiDungDTO> getNguoiDungMoi() { return nguoiDungMoi; }
    public void setNguoiDungMoi(List<NguoiDungDTO> nguoiDungMoi) { this.nguoiDungMoi = nguoiDungMoi; }

    public List<DeThiMoiDTO> getDeThiMoi() { return deThiMoi; }
    public void setDeThiMoi(List<DeThiMoiDTO> deThiMoi) { this.deThiMoi = deThiMoi; }

    // ===== INNER DTO =====

    /** DTO - Thống kê thi theo tháng */
    public static class ThongKeThangDTO {
        private String thang;      // vd: "01/2025"
        private long soLuotThi;
        private BigDecimal diemTrungBinh;

        public String getThang() { return thang; }
        public void setThang(String thang) { this.thang = thang; }
        public long getSoLuotThi() { return soLuotThi; }
        public void setSoLuotThi(long soLuotThi) { this.soLuotThi = soLuotThi; }
        public BigDecimal getDiemTrungBinh() { return diemTrungBinh; }
        public void setDiemTrungBinh(BigDecimal diemTrungBinh) { this.diemTrungBinh = diemTrungBinh; }
    }

    /** DTO - Thống kê theo môn học */
    public static class MonHocThongKeDTO {
        private String tenMonHoc;
        private long soDeThi;
        private long soCauHoi;
        private long soLuotThi;
        private BigDecimal diemTrungBinh;

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

    /** DTO - Giáo viên tích cực */
    public static class GiaoVienDTO {
        private String id;
        private String hoTen;
        private String email;
        private long soDeThi;
        private long soCauHoi;
        private long soLuotThi;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getHoTen() { return hoTen; }
        public void setHoTen(String hoTen) { this.hoTen = hoTen; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public long getSoDeThi() { return soDeThi; }
        public void setSoDeThi(long soDeThi) { this.soDeThi = soDeThi; }
        public long getSoCauHoi() { return soCauHoi; }
        public void setSoCauHoi(long soCauHoi) { this.soCauHoi = soCauHoi; }
        public long getSoLuotThi() { return soLuotThi; }
        public void setSoLuotThi(long soLuotThi) { this.soLuotThi = soLuotThi; }
    }

    /** DTO - Sinh viên xuất sắc */
    public static class SinhVienDTO {
        private String id;
        private String hoTen;
        private String email;
        private BigDecimal diemTrungBinh;
        private long soLanThi;
        private Integer xepHang;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getHoTen() { return hoTen; }
        public void setHoTen(String hoTen) { this.hoTen = hoTen; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public BigDecimal getDiemTrungBinh() { return diemTrungBinh; }
        public void setDiemTrungBinh(BigDecimal diemTrungBinh) { this.diemTrungBinh = diemTrungBinh; }
        public long getSoLanThi() { return soLanThi; }
        public void setSoLanThi(long soLanThi) { this.soLanThi = soLanThi; }
        public Integer getXepHang() { return xepHang; }
        public void setXepHang(Integer xepHang) { this.xepHang = xepHang; }
    }

    /** DTO - Đề thi mới tạo */
    public static class DeThiMoiDTO {
        private String id;
        private String tenDeThi;
        private String tenMonHoc;
        private String tenGiaoVien;
        private String thoiGianTao;
        private long soLuotThi;
        private String trangThai;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTenDeThi() { return tenDeThi; }
        public void setTenDeThi(String tenDeThi) { this.tenDeThi = tenDeThi; }
        public String getTenMonHoc() { return tenMonHoc; }
        public void setTenMonHoc(String tenMonHoc) { this.tenMonHoc = tenMonHoc; }
        public String getTenGiaoVien() { return tenGiaoVien; }
        public void setTenGiaoVien(String tenGiaoVien) { this.tenGiaoVien = tenGiaoVien; }
        public String getThoiGianTao() { return thoiGianTao; }
        public void setThoiGianTao(String thoiGianTao) { this.thoiGianTao = thoiGianTao; }
        public long getSoLuotThi() { return soLuotThi; }
        public void setSoLuotThi(long soLuotThi) { this.soLuotThi = soLuotThi; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }
}
