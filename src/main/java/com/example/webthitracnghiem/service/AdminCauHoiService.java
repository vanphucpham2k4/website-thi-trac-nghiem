package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.model.*;
import com.example.webthitracnghiem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service — Quản lý Câu Hỏi cho Admin (xem toàn bộ hệ thống, xóa).
 */
@Service
public class AdminCauHoiService {

    private final CauHoiRepository cauHoiRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final MonHocRepository monHocRepository;
    private final ChuDeRepository chuDeRepository;

    public AdminCauHoiService(CauHoiRepository cauHoiRepository,
                              NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                              MonHocRepository monHocRepository,
                              ChuDeRepository chuDeRepository) {
        this.cauHoiRepository = cauHoiRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.monHocRepository = monHocRepository;
        this.chuDeRepository = chuDeRepository;
    }

    // ================================================================
    // Cấp 1: Danh sách giảng viên + thống kê câu hỏi
    // ================================================================

    public ApiResponse<List<AdminGiaoVienCauHoiSummaryDTO>> layDanhSachGiaoVienCauHoi() {
        try {
            // Lấy tất cả user có vai trò GIAO_VIEN
            List<NguoiDungVaiTro> giaoVienVaiTroList = nguoiDungVaiTroRepository.findAll().stream()
                    .filter(ndvt -> ndvt.getVaiTro() != null
                            && AuthService.ROLE_GIAO_VIEN.equals(ndvt.getVaiTro().getTenVaiTro()))
                    .toList();

            // Đếm câu hỏi theo GV + độ khó
            Map<String, Map<String, Long>> cauHoiStats = new HashMap<>();
            for (Object[] row : cauHoiRepository.adminDemCauHoiTheoGiaoVienVaDoKho()) {
                String gvId = (String) row[0];
                String doKho = (String) row[1];
                long count = (Long) row[2];
                cauHoiStats.computeIfAbsent(gvId, k -> new HashMap<>()).put(doKho != null ? doKho : "KHAC", count);
            }

            List<AdminGiaoVienCauHoiSummaryDTO> result = new ArrayList<>();
            for (NguoiDungVaiTro ndvt : giaoVienVaiTroList) {
                NguoiDung nd = ndvt.getNguoiDung();
                String gvId = nd.getId();

                AdminGiaoVienCauHoiSummaryDTO dto = new AdminGiaoVienCauHoiSummaryDTO();
                dto.setNguoiDungId(gvId);
                dto.setHoTen(nd.getHoTen() != null ? nd.getHoTen() : (nd.getHo() + " " + nd.getTen()));
                dto.setEmail(nd.getEmail());

                Map<String, Long> stats = cauHoiStats.getOrDefault(gvId, Collections.emptyMap());
                long de = stats.getOrDefault("DE", 0L);
                long tb = stats.getOrDefault("TRUNG_BINH", 0L);
                long kho = stats.getOrDefault("KHO", 0L);
                long khac = stats.getOrDefault("KHAC", 0L);
                dto.setSoCauDe(de);
                dto.setSoCauTrungBinh(tb);
                dto.setSoCauKho(kho);
                dto.setTongCauHoi(de + tb + kho + khac);

                result.add(dto);
            }

            // Sắp xếp theo tổng câu hỏi giảm dần
            result.sort(Comparator.comparingLong(AdminGiaoVienCauHoiSummaryDTO::getTongCauHoi).reversed());

            return ApiResponse.success("Lấy danh sách giảng viên thành công.", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage(), AuthService.ERR_HE_THONG);
        }
    }

    // ================================================================
    // Cấp 2: Câu hỏi của 1 giảng viên
    // ================================================================

    public ApiResponse<List<AdminCauHoiItemDTO>> layCauHoiTheoGiaoVien(
            String nguoiDungId, String monHocId, String chuDeId, String doKho, String keyword) {
        try {
            String mh = (monHocId != null && monHocId.isBlank()) ? null : monHocId;
            String cd = (chuDeId != null && chuDeId.isBlank()) ? null : chuDeId;
            String dk = (doKho != null && doKho.isBlank()) ? null : doKho;
            String kw = (keyword != null && keyword.isBlank()) ? null : keyword;

            List<CauHoi> cauHoiList = cauHoiRepository.adminLocCauHoiTheoGiaoVien(nguoiDungId, mh, cd, dk, kw);

            List<AdminCauHoiItemDTO> result = cauHoiList.stream()
                    .map(this::chuyenDoiCauHoiDTO)
                    .toList();

            return ApiResponse.success("Lấy danh sách câu hỏi thành công.", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage(), AuthService.ERR_HE_THONG);
        }
    }

    // ================================================================
    // Chi tiết 1 câu hỏi
    // ================================================================

    public ApiResponse<AdminCauHoiItemDTO> layChiTietCauHoi(String cauHoiId) {
        try {
            Optional<CauHoi> opt = cauHoiRepository.findById(cauHoiId);
            if (opt.isEmpty()) {
                return ApiResponse.error("Câu hỏi không tồn tại.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
            }
            return ApiResponse.success("OK", chuyenDoiCauHoiDTO(opt.get()));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage(), AuthService.ERR_HE_THONG);
        }
    }

    // ================================================================
    // Xóa câu hỏi (kiểm tra constraint)
    // ================================================================

    @Transactional
    public ApiResponse<Void> xoaCauHoi(String cauHoiId) {
        try {
            Optional<CauHoi> opt = cauHoiRepository.findById(cauHoiId);
            if (opt.isEmpty()) {
                return ApiResponse.error("Câu hỏi không tồn tại.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
            }

            CauHoi ch = opt.get();

            // Kiểm tra đang dùng trong đề thi
            long soDeThiSuDung = cauHoiRepository.demSoDeThiSuDung(cauHoiId);
            if (soDeThiSuDung > 0) {
                return ApiResponse.error(
                        "Không thể xóa! Câu hỏi đang được sử dụng trong " + soDeThiSuDung + " đề thi.",
                        AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }

            cauHoiRepository.delete(ch);

            return ApiResponse.success("Đã xóa câu hỏi thành công.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Lỗi hệ thống: " + e.getMessage(), AuthService.ERR_HE_THONG);
        }
    }

    // ================================================================
    // Danh sách môn học & chủ đề (dropdown filter)
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

    public ApiResponse<List<Map<String, String>>> layDanhSachChuDe(String monHocId) {
        List<ChuDe> list = chuDeRepository.findByMonHocId(monHocId);
        List<Map<String, String>> result = list.stream()
                .map(cd -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("id", cd.getId());
                    m.put("ten", cd.getTen());
                    return m;
                }).toList();
        return ApiResponse.success("OK", result);
    }

    // ================================================================
    // Helper
    // ================================================================

    private AdminCauHoiItemDTO chuyenDoiCauHoiDTO(CauHoi ch) {
        AdminCauHoiItemDTO dto = new AdminCauHoiItemDTO();
        dto.setId(ch.getId());
        dto.setNoiDung(ch.getNoiDung());
        dto.setLoaiCauHoi(ch.getLoaiCauHoi());
        dto.setDoKho(ch.getDoKho());
        dto.setDapAnDung(ch.getDapAnDung());
        dto.setLuaChonA(ch.getLuaChonA());
        dto.setLuaChonB(ch.getLuaChonB());
        dto.setLuaChonC(ch.getLuaChonC());
        dto.setLuaChonD(ch.getLuaChonD());
        dto.setChuDeId(ch.getChuDe().getId());
        dto.setTenChuDe(ch.getChuDe().getTen());
        dto.setMonHocId(ch.getChuDe().getMonHoc().getId());
        dto.setTenMonHoc(ch.getChuDe().getMonHoc().getTen());

        NguoiDung gv = ch.getNguoiDung();
        dto.setNguoiTaoId(gv.getId());
        dto.setTenNguoiTao(gv.getHoTen() != null ? gv.getHoTen() : (gv.getHo() + " " + gv.getTen()));
        dto.setEmailNguoiTao(gv.getEmail());

        dto.setSoDeThiSuDung(cauHoiRepository.demSoDeThiSuDung(ch.getId()));

        return dto;
    }
}
