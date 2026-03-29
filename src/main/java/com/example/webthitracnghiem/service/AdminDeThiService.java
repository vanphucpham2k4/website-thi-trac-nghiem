package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.model.*;
import com.example.webthitracnghiem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service — Quản lý Đề Thi cho Admin (xem toàn bộ hệ thống, xóa).
 */
@Service
public class AdminDeThiService {

    private final DeThiRepository deThiRepository;
    private final DeThiCauHoiRepository deThiCauHoiRepository;
    private final PhienThiRepository phienThiRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final MonHocRepository monHocRepository;

    public AdminDeThiService(DeThiRepository deThiRepository,
                             DeThiCauHoiRepository deThiCauHoiRepository,
                             PhienThiRepository phienThiRepository,
                             NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                             MonHocRepository monHocRepository) {
        this.deThiRepository = deThiRepository;
        this.deThiCauHoiRepository = deThiCauHoiRepository;
        this.phienThiRepository = phienThiRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.monHocRepository = monHocRepository;
    }

    // ================================================================
    // Cấp 1: Danh sách giảng viên + thống kê đề thi
    // ================================================================

    public ApiResponse<List<AdminGiaoVienDeThiSummaryDTO>> layDanhSachGiaoVienDeThi() {
        try {
            // Lấy tất cả user có vai trò GIAO_VIEN
            List<NguoiDungVaiTro> giaoVienVaiTroList = nguoiDungVaiTroRepository.findAll().stream()
                    .filter(ndvt -> ndvt.getVaiTro() != null
                            && AuthService.ROLE_GIAO_VIEN.equals(ndvt.getVaiTro().getTenVaiTro()))
                    .toList();

            // Đếm đề thi theo GV + trạng thái
            Map<String, Map<String, Long>> deThiStats = new HashMap<>();
            for (Object[] row : deThiRepository.adminDemDeThiTheoGiaoVienVaTrangThai()) {
                String gvId = (String) row[0];
                String tt = (String) row[1];
                long count = (Long) row[2];
                deThiStats.computeIfAbsent(gvId, k -> new HashMap<>()).put(tt, count);
            }

            // Đếm số môn học theo GV
            Map<String, Long> monHocStats = new HashMap<>();
            for (Object[] row : deThiRepository.adminDemSoMonHocTheoGiaoVien()) {
                monHocStats.put((String) row[0], (Long) row[1]);
            }

            List<AdminGiaoVienDeThiSummaryDTO> result = new ArrayList<>();
            for (NguoiDungVaiTro ndvt : giaoVienVaiTroList) {
                NguoiDung nd = ndvt.getNguoiDung();
                String gvId = nd.getId();

                AdminGiaoVienDeThiSummaryDTO dto = new AdminGiaoVienDeThiSummaryDTO();
                dto.setNguoiDungId(gvId);
                dto.setHoTen(nd.getHoTen() != null ? nd.getHoTen() : (nd.getHo() + " " + nd.getTen()));
                dto.setEmail(nd.getEmail());

                Map<String, Long> stats = deThiStats.getOrDefault(gvId, Collections.emptyMap());
                long nhap = stats.getOrDefault("NHAP", 0L);
                long congKhai = stats.getOrDefault("CONG_KHAI", 0L);
                dto.setSoDeThiNhap(nhap);
                dto.setSoDeThiCongKhai(congKhai);
                dto.setTongDeThi(nhap + congKhai);
                dto.setSoMonHoc(monHocStats.getOrDefault(gvId, 0L));

                result.add(dto);
            }

            // Sắp xếp theo tổng đề thi giảm dần
            result.sort(Comparator.comparingLong(AdminGiaoVienDeThiSummaryDTO::getTongDeThi).reversed());

            return ApiResponse.success("Lấy danh sách giảng viên thành công.", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage(), AuthService.ERR_HE_THONG);
        }
    }

    // ================================================================
    // Cấp 2: Đề thi của 1 giảng viên
    // ================================================================

    public ApiResponse<List<AdminDeThiItemDTO>> layDeThiTheoGiaoVien(
            String nguoiDungId, String monHocId, String trangThai, String keyword) {
        try {
            String mh = (monHocId != null && monHocId.isBlank()) ? null : monHocId;
            String tt = (trangThai != null && trangThai.isBlank()) ? null : trangThai;
            String kw = (keyword != null && keyword.isBlank()) ? null : keyword;

            List<DeThi> deThiList = deThiRepository.adminLocDeThiTheoGiaoVien(nguoiDungId, mh, tt, kw);

            List<AdminDeThiItemDTO> result = deThiList.stream()
                    .map(this::chuyenDoiDeThiDTO)
                    .toList();

            return ApiResponse.success("Lấy danh sách đề thi thành công.", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage(), AuthService.ERR_HE_THONG);
        }
    }

    // ================================================================
    // Chi tiết 1 đề thi (bao gồm danh sách CH)
    // ================================================================

    public ApiResponse<AdminDeThiItemDTO> layChiTietDeThi(String deThiId) {
        try {
            Optional<DeThi> opt = deThiRepository.findById(deThiId);
            if (opt.isEmpty()) {
                return ApiResponse.error("Đề thi không tồn tại.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
            }

            DeThi dt = opt.get();
            AdminDeThiItemDTO dto = chuyenDoiDeThiDTO(dt);

            // Lấy danh sách câu hỏi trong đề
            List<DeThiCauHoi> deThiCauHois = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(dt);
            List<CauHoiListItemDTO> cauHoiDTOs = deThiCauHois.stream().map(dtch -> {
                CauHoi ch = dtch.getCauHoi();
                CauHoiListItemDTO chDto = new CauHoiListItemDTO();
                chDto.setId(ch.getId());
                chDto.setNoiDung(ch.getNoiDung());
                chDto.setLoaiCauHoi(ch.getLoaiCauHoi());
                chDto.setDoKho(ch.getDoKho());
                chDto.setDapAnDung(ch.getDapAnDung());
                chDto.setLuaChonA(ch.getLuaChonA());
                chDto.setLuaChonB(ch.getLuaChonB());
                chDto.setLuaChonC(ch.getLuaChonC());
                chDto.setLuaChonD(ch.getLuaChonD());
                chDto.setChuDeId(ch.getChuDe().getId());
                chDto.setTenChuDe(ch.getChuDe().getTen());
                chDto.setMonHocId(ch.getChuDe().getMonHoc().getId());
                chDto.setTenMonHoc(ch.getChuDe().getMonHoc().getTen());
                return chDto;
            }).toList();

            dto.setDanhSachCauHoi(cauHoiDTOs);

            return ApiResponse.success("Lấy chi tiết đề thi thành công.", dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage(), AuthService.ERR_HE_THONG);
        }
    }

    // ================================================================
    // Xóa hẳn đề thi (kiểm tra constraint phiên thi)
    // ================================================================

    @Transactional
    public ApiResponse<Void> xoaHanDeThi(String deThiId) {
        try {
            Optional<DeThi> opt = deThiRepository.findById(deThiId);
            if (opt.isEmpty()) {
                return ApiResponse.error("Đề thi không tồn tại.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
            }

            DeThi dt = opt.get();

            // Kiểm tra đã có phiên thi chưa
            if (phienThiRepository.existsByDeThi(dt)) {
                return ApiResponse.error(
                        "Không thể xóa! Đề thi đã có sinh viên làm bài. Hãy liên hệ giáo viên để xóa mềm.",
                        AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }

            // Xóa liên kết câu hỏi trong đề
            deThiCauHoiRepository.deleteByDeThi(dt);

            // Xóa đề thi
            deThiRepository.delete(dt);

            return ApiResponse.success("Đã xóa đề thi thành công.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage(), AuthService.ERR_HE_THONG);
        }
    }

    // ================================================================
    // Danh sách môn học (dropdown filter)
    // ================================================================

    public ApiResponse<List<Map<String, String>>> layDanhSachMonHoc() {
        List<Map<String, String>> result = monHocRepository.findAll().stream()
                .map(mh -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("id", mh.getId());
                    m.put("ten", mh.getTen());
                    return m;
                }).toList();
        return ApiResponse.success("OK", result);
    }

    // ================================================================
    // Helper
    // ================================================================

    private AdminDeThiItemDTO chuyenDoiDeThiDTO(DeThi dt) {
        AdminDeThiItemDTO dto = new AdminDeThiItemDTO();
        dto.setId(dt.getId());
        dto.setMaDeThi(dt.getMaDeThi());
        dto.setTenDeThi(dt.getTen());
        dto.setMonHocId(dt.getMonHoc().getId());
        dto.setTenMonHoc(dt.getMonHoc().getTen());
        dto.setThoiGianPhut(dt.getThoiGianPhut());
        dto.setMoTa(dt.getMoTa());
        dto.setTrangThai(dt.getTrangThai());
        dto.setThoiGianMo(dt.getThoiGianMo());
        dto.setThoiGianDong(dt.getThoiGianDong());
        dto.setSoLanThiToiDa(dt.getSoLanThiToiDa());
        dto.setTronCauHoi(dt.getTronCauHoi());
        dto.setTronDapAn(dt.getTronDapAn());
        dto.setChoPhepXemLai(dt.getChoPhepXemLai());
        dto.setThoiGianTao(dt.getThoiGianTao());

        dto.setSoCauHoi(deThiCauHoiRepository.countByDeThi(dt));
        dto.setSoLuotThi(phienThiRepository.findByDeThi(dt).size());

        NguoiDung gv = dt.getNguoiDung();
        dto.setNguoiTaoId(gv.getId());
        dto.setTenNguoiTao(gv.getHoTen() != null ? gv.getHoTen() : (gv.getHo() + " " + gv.getTen()));

        return dto;
    }
}
