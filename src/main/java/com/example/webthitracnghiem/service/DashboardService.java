package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.AdminDashboardDTO;
import com.example.webthitracnghiem.dto.GiaoVienDashboardDTO;
import com.example.webthitracnghiem.dto.NguoiDungDTO;
import com.example.webthitracnghiem.dto.SinhVienDashboardDTO;
import com.example.webthitracnghiem.model.DeThi;
import com.example.webthitracnghiem.model.KetQuaThi;
import com.example.webthitracnghiem.model.MonHoc;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.PhienThi;
import com.example.webthitracnghiem.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service - Xử lý dữ liệu Dashboard cho Sinh Viên và Giáo Viên
 * Cung cấp các phương thức lấy thống kê, bảng điểm, hoạt động gần đây...
 */
@Service
public class DashboardService {

    // ===== CÁC BEAN DEPENDENCY INJECTION =====
    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final DeThiRepository deThiRepository;
    private final PhienThiRepository phienThiRepository;
    private final KetQuaThiRepository ketQuaThiRepository;
    private final MonHocRepository monHocRepository;

    /**
     * Constructor injection - Tiêm các repository
     */
    public DashboardService(NguoiDungRepository nguoiDungRepository,
                           NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                           DeThiRepository deThiRepository,
                           PhienThiRepository phienThiRepository,
                           KetQuaThiRepository ketQuaThiRepository,
                           MonHocRepository monHocRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.deThiRepository = deThiRepository;
        this.phienThiRepository = phienThiRepository;
        this.ketQuaThiRepository = ketQuaThiRepository;
        this.monHocRepository = monHocRepository;
    }

    /**
     * Kiểm tra người dùng có vai trò cụ thể không (qua bảng nguoi_dung_vai_tro)
     */
    private boolean nguoiDungCoVaiTro(NguoiDung nd, String tenVaiTro) {
        if (nd == null || nd.getId() == null) {
            return false;
        }
        return nguoiDungVaiTroRepository.findByNguoiDungId(nd.getId()).stream()
                .map(ndvt -> ndvt.getVaiTro() != null ? ndvt.getVaiTro().getTenVaiTro() : null)
                .filter(Objects::nonNull)
                .anyMatch(tenVaiTro::equals);
    }

    /**
     * Lấy tên vai trò đầu tiên khác ADMIN (để hiển thị đăng ký SV/GV)
     */
    private String layVaiTroKhongAdminDauTien(NguoiDung nd) {
        if (nd == null || nd.getId() == null) {
            return "N/A";
        }
        return nguoiDungVaiTroRepository.findByNguoiDungId(nd.getId()).stream()
                .map(ndvt -> ndvt.getVaiTro() != null ? ndvt.getVaiTro().getTenVaiTro() : null)
                .filter(Objects::nonNull)
                .filter(vt -> !"ADMIN".equals(vt))
                .findFirst()
                .orElse("N/A");
    }

    /**
     * Có ít nhất một vai trò không phải ADMIN (dùng cho danh sách người dùng mới)
     */
    private boolean nguoiDungCoVaiTroKhongPhaiAdmin(NguoiDung nd) {
        if (nd == null || nd.getId() == null) {
            return false;
        }
        return nguoiDungVaiTroRepository.findByNguoiDungId(nd.getId()).stream()
                .map(ndvt -> ndvt.getVaiTro() != null ? ndvt.getVaiTro().getTenVaiTro() : null)
                .filter(Objects::nonNull)
                .anyMatch(vt -> !"ADMIN".equals(vt));
    }

    // ========================================
    // 1. DASHBOARD SINH VIÊN
    // ========================================

    /**
     * Lấy dữ liệu dashboard cho SINH VIÊN
     *
     * @param nguoiDungId ID của sinh viên cần lấy dữ liệu
     * @return SinhVienDashboardDTO chứa toàn bộ thông tin thống kê
     */
    public SinhVienDashboardDTO layDashboardSinhVien(String nguoiDungId) {
        // Tìm sinh viên theo ID
        Optional<NguoiDung> optionalSinhVien = nguoiDungRepository.findById(nguoiDungId);

        if (optionalSinhVien.isEmpty()) {
            return null; // Không tìm thấy sinh viên
        }

        NguoiDung sinhVien = optionalSinhVien.get();
        SinhVienDashboardDTO dto = new SinhVienDashboardDTO();

        // ===== THÔNG TIN CÁ NHÂN =====
        dto.setId(sinhVien.getId());
        dto.setMaNguoiDung(sinhVien.getMaNguoiDung());
        dto.setHoTen(sinhVien.getHoTen());
        dto.setEmail(sinhVien.getEmail());
        dto.setSoDienThoai(sinhVien.getSoDienThoai());

        // ===== THỐNG KÊ TỔNG QUAN =====
        // Tổng số lần thi
        long tongSoLanThi = phienThiRepository.demTongSoLanThi(sinhVien);
        dto.setTongSoLanThi(tongSoLanThi);

        // Số bài thi đã hoàn thành
        long soBaiHoanThanh = ketQuaThiRepository.demSoBaiThiHoanThanh(sinhVien);
        dto.setSoBaiThiHoanThanh(soBaiHoanThanh);

        // Điểm trung bình
        BigDecimal diemTB = ketQuaThiRepository.tinhDiemTrungBinh(sinhVien);
        dto.setDiemTrungBinh(diemTB != null ? diemTB.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // Số môn học đã tham gia
        Set<String> monHocSet = layDanhSachMonHocDaThi(sinhVien);
        dto.setSoMonHocThamGia(monHocSet.size());

        // Xếp hạng (tính đơn giản)
        dto.setXepHang(tinhXepHang(sinhVien, diemTB));

        // ===== ĐIỂM THEO TỪNG MÔN =====
        dto.setDiemTheoMon(layDiemTheoMon(sinhVien));

        // ===== BÀI THI GẦN NHẤT =====
        dto.setBaiThiGanNhat(layBaiThiGanNhat(sinhVien));

        return dto;
    }

    /**
     * Lấy danh sách môn học đã thi của sinh viên
     */
    private Set<String> layDanhSachMonHocDaThi(NguoiDung sinhVien) {
        return phienThiRepository.findByNguoiDung(sinhVien)
                .stream()
                .map(phien -> phien.getDeThi().getMonHoc().getTen())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Tính xếp hạng đơn giản (dựa trên điểm trung bình)
     */
    private Integer tinhXepHang(NguoiDung sinhVien, BigDecimal diemTB) {
        if (diemTB == null) return null;

        // Đếm số sinh viên có điểm cao hơn
        List<NguoiDung> tatCaSV = nguoiDungRepository.findAll();
        int xepHang = 1;

        for (NguoiDung sv : tatCaSV) {
            BigDecimal diemSVTB = ketQuaThiRepository.tinhDiemTrungBinh(sv);
            if (diemSVTB != null && diemSVTB.compareTo(diemTB) > 0) {
                xepHang++;
            }
        }

        return xepHang;
    }

    /**
     * Lấy điểm theo từng môn học
     */
    private List<SinhVienDashboardDTO.MonDiemDTO> layDiemTheoMon(NguoiDung sinhVien) {
        // Lấy tất cả kết quả thi của sinh viên
        List<KetQuaThi> ketQuaList = ketQuaThiRepository.findByNguoiDung(sinhVien);

        // Nhóm theo môn học
        Map<String, List<KetQuaThi>> nhomTheoMon = ketQuaList.stream()
                .collect(Collectors.groupingBy(
                        kq -> kq.getPhienThi().getDeThi().getMonHoc().getTen()
                ));

        // Tạo DTO cho từng môn
        List<SinhVienDashboardDTO.MonDiemDTO> danhSachMon = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Map.Entry<String, List<KetQuaThi>> entry : nhomTheoMon.entrySet()) {
            SinhVienDashboardDTO.MonDiemDTO monDiem = new SinhVienDashboardDTO.MonDiemDTO();
            monDiem.setTenMon(entry.getKey());
            monDiem.setSoLanThi(entry.getValue().size());

            // Tính điểm cao nhất, thấp nhất, trung bình
            List<BigDecimal> diemList = entry.getValue().stream()
                    .map(KetQuaThi::getTongDiem)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!diemList.isEmpty()) {
                BigDecimal maxDiem = diemList.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal minDiem = diemList.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal avgDiem = diemList.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(diemList.size()), 2, RoundingMode.HALF_UP);

                monDiem.setDiemCaoNhat(maxDiem.setScale(2, RoundingMode.HALF_UP));
                monDiem.setDiemThapNhat(minDiem.setScale(2, RoundingMode.HALF_UP));
                monDiem.setDiemTrungBinhMon(avgDiem);
            }

            danhSachMon.add(monDiem);
        }

        return danhSachMon;
    }

    /**
     * Lấy bài thi gần nhất
     */
    private SinhVienDashboardDTO.KetQuaGanNhatDTO layBaiThiGanNhat(NguoiDung sinhVien) {
        List<KetQuaThi> ketQuaList = ketQuaThiRepository.findByNguoiDung(sinhVien);

        if (ketQuaList.isEmpty()) {
            return null;
        }

        // Lấy kết quả mới nhất
        KetQuaThi ketQuaGanNhat = ketQuaList.get(0);
        PhienThi phienThi = ketQuaGanNhat.getPhienThi();
        DeThi deThi = phienThi.getDeThi();

        SinhVienDashboardDTO.KetQuaGanNhatDTO dto = new SinhVienDashboardDTO.KetQuaGanNhatDTO();
        dto.setTenDeThi(deThi.getTen());
        dto.setTenMonHoc(deThi.getMonHoc().getTen());
        dto.setDiem(ketQuaGanNhat.getTongDiem());
        dto.setNgayThi(ketQuaGanNhat.getThoiGianNop() != null
                ? ketQuaGanNhat.getThoiGianNop().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "N/A");
        dto.setTrangThaiCham(ketQuaGanNhat.getTrangThaiCham() != null
                ? ketQuaGanNhat.getTrangThaiCham()
                : "Đã chấm");

        return dto;
    }

    // ========================================
    // 2. DASHBOARD GIÁO VIÊN
    // ========================================

    /**
     * Lấy dữ liệu dashboard cho GIÁO VIÊN
     *
     * @param nguoiDungId ID của giáo viên cần lấy dữ liệu
     * @return GiaoVienDashboardDTO chứa toàn bộ thông tin thống kê
     */
    public GiaoVienDashboardDTO layDashboardGiaoVien(String nguoiDungId) {
        // Tìm giáo viên theo ID
        Optional<NguoiDung> optionalGiaoVien = nguoiDungRepository.findById(nguoiDungId);

        if (optionalGiaoVien.isEmpty()) {
            return null;
        }

        NguoiDung giaoVien = optionalGiaoVien.get();
        GiaoVienDashboardDTO dto = new GiaoVienDashboardDTO();

        // ===== THÔNG TIN CÁ NHÂN =====
        dto.setId(giaoVien.getId());
        dto.setMaNguoiDung(giaoVien.getMaNguoiDung());
        dto.setHoTen(giaoVien.getHoTen());
        dto.setEmail(giaoVien.getEmail());
        dto.setSoDienThoai(giaoVien.getSoDienThoai());

        // ===== THỐNG KÊ TỔNG QUAN =====
        // Tổng số đề thi đã tạo
        List<DeThi> danhSachDeThi = deThiRepository.findByNguoiDung(giaoVien);
        dto.setTongSoDeThi(danhSachDeThi.size());

        // Tổng số lượt thi (từ tất cả các đề thi)
        long tongLuotThi = 0;
        Set<String> sinhVienSet = new HashSet<>();

        for (DeThi deThi : danhSachDeThi) {
            List<PhienThi> phienThiList = phienThiRepository.findByDeThi(deThi);
            tongLuotThi += phienThiList.size();

            // Thu thập danh sách sinh viên
            phienThiList.forEach(p -> sinhVienSet.add(p.getNguoiDung().getId()));
        }

        dto.setTongSoLuotThi(tongLuotThi);
        dto.setTongSoSinhVien(sinhVienSet.size());

        // Điểm trung bình chung (từ tất cả các bài thi)
        BigDecimal tongDiem = BigDecimal.ZERO;
        long soDiem = 0;

        for (DeThi deThi : danhSachDeThi) {
            List<KetQuaThi> ketQuaList = ketQuaThiRepository.findByNguoiDung(giaoVien);
            for (KetQuaThi kq : ketQuaList) {
                if (kq.getTongDiem() != null) {
                    tongDiem = tongDiem.add(kq.getTongDiem());
                    soDiem++;
                }
            }
        }

        if (soDiem > 0) {
            dto.setDiemTrungBinhChung(tongDiem.divide(BigDecimal.valueOf(soDiem), 2, RoundingMode.HALF_UP));
        } else {
            dto.setDiemTrungBinhChung(BigDecimal.ZERO);
        }

        // ===== ĐỀ THI GẦN NHẤT =====
        if (!danhSachDeThi.isEmpty()) {
            dto.setDeThiGanNhat(layDeThiGanNhat(danhSachDeThi.get(0)));
        }

        // ===== DANH SÁCH ĐỀ THI GẦN ĐÂY (5 cái) =====
        dto.setDeThiGanDay(danhSachDeThi.stream()
                .limit(5)
                .map(this::chuyenDoiDeThiDTO)
                .collect(Collectors.toList()));

        // ===== THỐNG KÊ THEO MÔN HỌC =====
        dto.setThongKeTheoMon(layThongKeTheoMon(giaoVien));

        return dto;
    }

    /**
     * Chuyển đổi DeThi entity sang DeThiDTO
     */
    private GiaoVienDashboardDTO.DeThiDTO chuyenDoiDeThiDTO(DeThi deThi) {
        GiaoVienDashboardDTO.DeThiDTO dto = new GiaoVienDashboardDTO.DeThiDTO();
        dto.setId(deThi.getId());
        dto.setMaDeThi(deThi.getMaDeThi());
        dto.setTenDeThi(deThi.getTen());
        dto.setTenMonHoc(deThi.getMonHoc().getTen());
        dto.setThoiGianPhut(deThi.getThoiGianPhut());
        dto.setMoTa(deThi.getMoTa());
        dto.setSoLuotThi(phienThiRepository.findByDeThi(deThi).size());

        // Xác định trạng thái
        if (deThi.getThoiGianMo() != null && deThi.getThoiGianDong() != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (now.isBefore(deThi.getThoiGianMo())) {
                dto.setTrangThai("Chưa mở");
            } else if (now.isAfter(deThi.getThoiGianDong())) {
                dto.setTrangThai("Đã đóng");
            } else {
                dto.setTrangThai("Đang mở");
            }
        } else {
            dto.setTrangThai("Đang mở");
        }

        return dto;
    }

    /**
     * Lấy thông tin đề thi gần nhất
     */
    private GiaoVienDashboardDTO.DeThiGanNhatDTO layDeThiGanNhat(DeThi deThi) {
        GiaoVienDashboardDTO.DeThiGanNhatDTO dto = new GiaoVienDashboardDTO.DeThiGanNhatDTO();
        dto.setId(deThi.getId());
        dto.setTenDeThi(deThi.getTen());
        dto.setTenMonHoc(deThi.getMonHoc().getTen());
        dto.setThoiGianPhut(deThi.getThoiGianPhut());
        dto.setSoLuotThi(phienThiRepository.findByDeThi(deThi).size());

        // Tính điểm trung bình
        List<PhienThi> phienThiList = phienThiRepository.findByDeThi(deThi);
        if (!phienThiList.isEmpty()) {
            BigDecimal tongDiem = BigDecimal.ZERO;
            int soDiem = 0;
            for (PhienThi pt : phienThiList) {
                if (pt.getThoiGianNop() != null) {
                    Optional<KetQuaThi> kq = ketQuaThiRepository.findById(pt.getId());
                    if (kq.isPresent() && kq.get().getTongDiem() != null) {
                        tongDiem = tongDiem.add(kq.get().getTongDiem());
                        soDiem++;
                    }
                }
            }
            if (soDiem > 0) {
                dto.setDiemTrungBinh(tongDiem.divide(BigDecimal.valueOf(soDiem), 2, RoundingMode.HALF_UP));
            }
        }

        // Trạng thái
        if (deThi.getThoiGianMo() != null && deThi.getThoiGianDong() != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (now.isBefore(deThi.getThoiGianMo())) {
                dto.setTrangThai("Chưa mở");
            } else if (now.isAfter(deThi.getThoiGianDong())) {
                dto.setTrangThai("Đã đóng");
            } else {
                dto.setTrangThai("Đang mở");
            }
        } else {
            dto.setTrangThai("Đang mở");
        }

        return dto;
    }

    /**
     * Lấy thống kê theo từng môn học
     */
    private List<GiaoVienDashboardDTO.MonHocThongKeDTO> layThongKeTheoMon(NguoiDung giaoVien) {
        List<DeThi> danhSachDeThi = deThiRepository.findByNguoiDung(giaoVien);

        // Nhóm đề thi theo môn học
        Map<String, List<DeThi>> nhomTheoMon = danhSachDeThi.stream()
                .collect(Collectors.groupingBy(d -> d.getMonHoc().getTen()));

        List<GiaoVienDashboardDTO.MonHocThongKeDTO> thongKeList = new ArrayList<>();

        for (Map.Entry<String, List<DeThi>> entry : nhomTheoMon.entrySet()) {
            GiaoVienDashboardDTO.MonHocThongKeDTO monThongKe = new GiaoVienDashboardDTO.MonHocThongKeDTO();
            monThongKe.setTenMonHoc(entry.getKey());
            monThongKe.setSoDeThi(entry.getValue().size());

            // Đếm lượt thi và thu thập điểm
            long tongLuot = 0;
            List<BigDecimal> diemList = new ArrayList<>();

            for (DeThi deThi : entry.getValue()) {
                List<PhienThi> phienThiList = phienThiRepository.findByDeThi(deThi);
                tongLuot += phienThiList.size();

                for (PhienThi pt : phienThiList) {
                    if (pt.getThoiGianNop() != null) {
                        Optional<KetQuaThi> kq = ketQuaThiRepository.findById(pt.getId());
                        kq.ifPresent(k -> {
                            if (k.getTongDiem() != null) {
                                diemList.add(k.getTongDiem());
                            }
                        });
                    }
                }
            }

            monThongKe.setSoLuotThi(tongLuot);

            if (!diemList.isEmpty()) {
                BigDecimal avg = diemList.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(diemList.size()), 2, RoundingMode.HALF_UP);
                monThongKe.setDiemTrungBinh(avg);
            }

            thongKeList.add(monThongKe);
        }

        return thongKeList;
    }

    // ========================================
    // 3. DASHBOARD ADMIN (QUẢN TRỊ VIÊN)
    // ========================================

    /**
     * Lấy dữ liệu dashboard cho ADMIN
     * Thống kê toàn bộ hệ thống: người dùng, đề thi, môn học...
     *
     * @param nguoiDungId ID của admin đang đăng nhập
     * @return AdminDashboardDTO chứa toàn bộ thông tin thống kê hệ thống
     */
    public AdminDashboardDTO layDashboardAdmin(String nguoiDungId) {
        Optional<NguoiDung> optionalAdmin = nguoiDungRepository.findById(nguoiDungId);

        if (optionalAdmin.isEmpty()) {
            return null;
        }

        NguoiDung admin = optionalAdmin.get();
        AdminDashboardDTO dto = new AdminDashboardDTO();

        // ===== THÔNG TIN CÁ NHÂN =====
        dto.setId(admin.getId());
        dto.setMaNguoiDung(admin.getMaNguoiDung());
        dto.setHoTen(admin.getHoTen());
        dto.setEmail(admin.getEmail());
        dto.setSoDienThoai(admin.getSoDienThoai());

        // ===== THỐNG KÊ TỔNG QUAN =====
        List<NguoiDung> tatCaNguoiDung = nguoiDungRepository.findAll();
        dto.setTongSoNguoiDung(tatCaNguoiDung.size());

        // Đếm theo vai trò (qua bảng nguoi_dung_vai_tro)
        dto.setTongSoSinhVien(tatCaNguoiDung.stream()
                .filter(nd -> nguoiDungCoVaiTro(nd, "SINH_VIEN"))
                .count());
        dto.setTongSoGiaoVien(tatCaNguoiDung.stream()
                .filter(nd -> nguoiDungCoVaiTro(nd, "GIAO_VIEN"))
                .count());

        // Các thống kê khác
        dto.setTongSoMonHoc(monHocRepository.count());
        dto.setTongSoDeThi(deThiRepository.count());
        dto.setTongSoLuotThi(phienThiRepository.count());

        // Tổng số câu hỏi (mock data vì chưa có CauHoi entity)
        dto.setTongSoCauHoi(0);

        // ===== THỐNG KÊ ĐIỂM =====
        // Điểm trung bình hệ thống
        BigDecimal tongDiem = BigDecimal.ZERO;
        int soDiem = 0;
        long soDo = 0;

        for (NguoiDung nd : tatCaNguoiDung) {
            List<KetQuaThi> ketQuaList = ketQuaThiRepository.findByNguoiDung(nd);
            for (KetQuaThi kq : ketQuaList) {
                if (kq.getTongDiem() != null) {
                    tongDiem = tongDiem.add(kq.getTongDiem());
                    soDiem++;
                    if (kq.getTongDiem().compareTo(BigDecimal.valueOf(5)) >= 0) {
                        soDo++;
                    }
                }
            }
        }

        if (soDiem > 0) {
            dto.setDiemTrungBinhHeThong(tongDiem.divide(BigDecimal.valueOf(soDiem), 2, RoundingMode.HALF_UP));
            dto.setTiLeDo(BigDecimal.valueOf(soDo).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(soDiem), 2, RoundingMode.HALF_UP));
        } else {
            dto.setDiemTrungBinhHeThong(BigDecimal.ZERO);
            dto.setTiLeDo(BigDecimal.ZERO);
        }

        // ===== BIỂU ĐỒ THỐNG KÊ THEO THÁNG (mock) =====
        dto.setThongKeTheoThang(taoDuLieuThongKeThang());

        // ===== THỐNG KÊ THEO MÔN HỌC =====
        dto.setThongKeTheoMon(taoThongKeTheoMon());

        // ===== TOP GIÁO VIÊN =====
        dto.setTopGiaoVien(taoTopGiaoVien(tatCaNguoiDung));

        // ===== TOP SINH VIÊN =====
        dto.setTopSinhVien(taoTopSinhVien(tatCaNguoiDung));

        // ===== NGƯỜI DÙNG MỚI (5 người gần nhất) =====
        dto.setNguoiDungMoi(tatCaNguoiDung.stream()
                .filter(this::nguoiDungCoVaiTroKhongPhaiAdmin)
                .sorted((a, b) -> {
                    if (a.getId() == null || b.getId() == null) return 0;
                    return b.getId().compareTo(a.getId());
                })
                .limit(5)
                .map(nd -> {
                    NguoiDungDTO ndDto = new NguoiDungDTO();
                    ndDto.setId(nd.getId());
                    ndDto.setHoTen(nd.getHoTen());
                    ndDto.setEmail(nd.getEmail());
                    ndDto.setSoDienThoai(nd.getSoDienThoai());
                    ndDto.setVaiTro(layVaiTroKhongAdminDauTien(nd));
                    return ndDto;
                })
                .collect(Collectors.toList()));

        // ===== ĐỀ THI MỚI (5 cái gần nhất) =====
        List<DeThi> deThiList = deThiRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        dto.setDeThiMoi(deThiList.stream()
                .sorted((a, b) -> {
                    if (a.getId() == null || b.getId() == null) return 0;
                    return b.getId().compareTo(a.getId());
                })
                .limit(5)
                .map(dt -> {
                    AdminDashboardDTO.DeThiMoiDTO deMoi = new AdminDashboardDTO.DeThiMoiDTO();
                    deMoi.setId(dt.getId());
                    deMoi.setTenDeThi(dt.getTen());
                    deMoi.setTenMonHoc(dt.getMonHoc() != null ? dt.getMonHoc().getTen() : "N/A");
                    deMoi.setTenGiaoVien(dt.getNguoiDung() != null ? dt.getNguoiDung().getHoTen() : "N/A");
                    // Entity DeThi không có thoiGianTao — dùng thời gian mở hoặc đóng để hiển thị
                    java.time.LocalDateTime thoiHienThi = dt.getThoiGianMo() != null
                            ? dt.getThoiGianMo()
                            : dt.getThoiGianDong();
                    deMoi.setThoiGianTao(thoiHienThi != null
                            ? thoiHienThi.format(formatter) : "N/A");
                    deMoi.setSoLuotThi(phienThiRepository.findByDeThi(dt).size());
                    if (dt.getThoiGianMo() != null && dt.getThoiGianDong() != null) {
                        java.time.LocalDateTime now = java.time.LocalDateTime.now();
                        if (now.isBefore(dt.getThoiGianMo())) {
                            deMoi.setTrangThai("Chưa mở");
                        } else if (now.isAfter(dt.getThoiGianDong())) {
                            deMoi.setTrangThai("Đã đóng");
                        } else {
                            deMoi.setTrangThai("Đang mở");
                        }
                    } else {
                        deMoi.setTrangThai("Đang mở");
                    }
                    return deMoi;
                })
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Tạo dữ liệu thống kê theo tháng (mock data 12 tháng)
     */
    private List<AdminDashboardDTO.ThongKeThangDTO> taoDuLieuThongKeThang() {
        List<AdminDashboardDTO.ThongKeThangDTO> result = new ArrayList<>();
        java.time.LocalDate now = java.time.LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            java.time.LocalDate thang = now.minusMonths(i);
            AdminDashboardDTO.ThongKeThangDTO item = new AdminDashboardDTO.ThongKeThangDTO();
            item.setThang(thang.format(DateTimeFormatter.ofPattern("MM/yyyy")));

            // Mock số lượt thi và điểm TB (trong thực tế lấy từ DB)
            item.setSoLuotThi((long) (50 + Math.random() * 200));
            item.setDiemTrungBinh(BigDecimal.valueOf(5 + Math.random() * 4)
                    .setScale(2, RoundingMode.HALF_UP));
            result.add(item);
        }

        return result;
    }

    /**
     * Tạo thống kê theo môn học
     */
    private List<AdminDashboardDTO.MonHocThongKeDTO> taoThongKeTheoMon() {
        List<AdminDashboardDTO.MonHocThongKeDTO> result = new ArrayList<>();
        List<MonHoc> monHocList = monHocRepository.findAll();

        for (MonHoc mh : monHocList) {
            AdminDashboardDTO.MonHocThongKeDTO item = new AdminDashboardDTO.MonHocThongKeDTO();
            item.setTenMonHoc(mh.getTen());

            List<DeThi> deThis = deThiRepository.findByMonHoc(mh);
            item.setSoDeThi(deThis.size());

            long luotThi = 0;
            for (DeThi dt : deThis) {
                luotThi += phienThiRepository.findByDeThi(dt).size();
            }
            item.setSoLuotThi(luotThi);

            result.add(item);
        }

        return result;
    }

    /**
     * Tạo top giáo viên tích cực
     */
    private List<AdminDashboardDTO.GiaoVienDTO> taoTopGiaoVien(List<NguoiDung> tatCaNguoiDung) {
        return tatCaNguoiDung.stream()
                .filter(nd -> nguoiDungCoVaiTro(nd, "GIAO_VIEN"))
                .sorted((a, b) -> {
                    long deA = deThiRepository.findByNguoiDung(a).size();
                    long deB = deThiRepository.findByNguoiDung(b).size();
                    return Long.compare(deB, deA);
                })
                .limit(5)
                .map(nd -> {
                    AdminDashboardDTO.GiaoVienDTO gv = new AdminDashboardDTO.GiaoVienDTO();
                    gv.setId(nd.getId());
                    gv.setHoTen(nd.getHoTen());
                    gv.setEmail(nd.getEmail());
                    List<DeThi> deCuaGv = deThiRepository.findByNguoiDung(nd);
                    gv.setSoDeThi(deCuaGv.size());
                    long tongLuot = 0;
                    for (DeThi d : deCuaGv) {
                        tongLuot += phienThiRepository.findByDeThi(d).size();
                    }
                    gv.setSoLuotThi(tongLuot);
                    gv.setSoCauHoi(0);
                    return gv;
                })
                .collect(Collectors.toList());
    }

    /**
     * Tạo top sinh viên xuất sắc
     */
    private List<AdminDashboardDTO.SinhVienDTO> taoTopSinhVien(List<NguoiDung> tatCaNguoiDung) {
        List<NguoiDung> sinhVienList = tatCaNguoiDung.stream()
                .filter(nd -> nguoiDungCoVaiTro(nd, "SINH_VIEN"))
                .sorted((a, b) -> {
                    BigDecimal diemA = ketQuaThiRepository.tinhDiemTrungBinh(a);
                    BigDecimal diemB = ketQuaThiRepository.tinhDiemTrungBinh(b);
                    if (diemA == null && diemB == null) return 0;
                    if (diemA == null) return 1;
                    if (diemB == null) return -1;
                    return diemB.compareTo(diemA);
                })
                .limit(5)
                .collect(Collectors.toList());

        List<AdminDashboardDTO.SinhVienDTO> result = new ArrayList<>();
        for (int i = 0; i < sinhVienList.size(); i++) {
            NguoiDung nd = sinhVienList.get(i);
            AdminDashboardDTO.SinhVienDTO sv = new AdminDashboardDTO.SinhVienDTO();
            sv.setId(nd.getId());
            sv.setHoTen(nd.getHoTen());
            sv.setEmail(nd.getEmail());
            BigDecimal diemTB = ketQuaThiRepository.tinhDiemTrungBinh(nd);
            sv.setDiemTrungBinh(diemTB != null ? diemTB.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            sv.setSoLanThi(ketQuaThiRepository.findByNguoiDung(nd).size());
            sv.setXepHang(i + 1);
            result.add(sv);
        }
        return result;
    }
}
