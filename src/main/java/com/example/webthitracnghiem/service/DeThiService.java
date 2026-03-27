package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.entity.*;
import com.example.webthitracnghiem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service — Quản lý Đề Thi (CRUD + Soft Delete + Hard Delete).
 *
 * Soft Delete: Đặt deleted_at = now() → ẩn khỏi danh sách bình thường nhưng giữ dữ liệu.
 * Hard Delete: Xóa hẳn — chỉ cho phép khi chưa có sinh viên nào làm bài (kiểm tra phien_thi).
 */
@Service
public class DeThiService {

    private final DeThiRepository deThiRepository;
    private final MonHocRepository monHocRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PhienThiRepository phienThiRepository;
    private final DeThiCauHoiRepository deThiCauHoiRepository;

    public DeThiService(DeThiRepository deThiRepository,
                        MonHocRepository monHocRepository,
                        NguoiDungRepository nguoiDungRepository,
                        PhienThiRepository phienThiRepository,
                        DeThiCauHoiRepository deThiCauHoiRepository) {
        this.deThiRepository = deThiRepository;
        this.monHocRepository = monHocRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.phienThiRepository = phienThiRepository;
        this.deThiCauHoiRepository = deThiCauHoiRepository;
    }

    // ================================================================
    // 1. LẤY DANH SÁCH ĐỀ THI
    // ================================================================

    /**
     * Lấy danh sách đề thi của một giáo viên (chưa bị xóa mềm).
     */
    public ApiResponse<List<DeThiListItemDTO>> layDanhSachDeThi(String nguoiDungId) {
        Optional<NguoiDung> optGiaoVien = nguoiDungRepository.findById(nguoiDungId);
        if (optGiaoVien.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        NguoiDung giaoVien = optGiaoVien.get();
        List<DeThi> danhSach = deThiRepository
                .findByNguoiDungAndDeletedAtIsNullOrderByThoiGianTaoDesc(giaoVien);

        List<DeThiListItemDTO> result = danhSach.stream()
                .map(this::chuyenDoiDeThiDTO)
                .toList();

        return ApiResponse.success("Lấy danh sách đề thi thành công", result);
    }

    /**
     * Lấy danh sách đề thi đã xóa mềm của giáo viên (thùng rác).
     */
    public ApiResponse<List<DeThiListItemDTO>> layDeThiDaXoa(String nguoiDungId) {
        Optional<NguoiDung> optGiaoVien = nguoiDungRepository.findById(nguoiDungId);
        if (optGiaoVien.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        List<DeThi> danhSach = deThiRepository
                .findByNguoiDungAndDeletedAtIsNotNull(optGiaoVien.get());

        List<DeThiListItemDTO> result = danhSach.stream()
                .map(this::chuyenDoiDeThiDTO)
                .toList();

        return ApiResponse.success("Lấy danh sách đề thi đã xóa thành công", result);
    }

    /**
     * Lấy chi tiết một đề thi (không bao gồm đề đã xóa mềm).
     */
    public ApiResponse<DeThiListItemDTO> layChiTietDeThi(String deThiId, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        DeThi deThi = optDeThi.get();
        // Kiểm tra quyền: chỉ người tạo mới được xem chi tiết qua API này
        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền xem đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        return ApiResponse.success("Lấy chi tiết đề thi thành công", chuyenDoiDeThiDTO(deThi));
    }

    // ================================================================
    // 2. TẠO ĐỀ THI MỚI
    // ================================================================

    @Transactional
    public ApiResponse<DeThiListItemDTO> taoDeThi(TaoDeThiDTO dto, String nguoiDungId) {
        // Tìm giáo viên
        Optional<NguoiDung> optGiaoVien = nguoiDungRepository.findById(nguoiDungId);
        if (optGiaoVien.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        // Tìm môn học
        Optional<MonHoc> optMonHoc = monHocRepository.findById(dto.getMonHocId());
        if (optMonHoc.isEmpty()) {
            return ApiResponse.error("Môn học không tồn tại!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        // Kiểm tra trạng thái hợp lệ
        String trangThai = dto.getTrangThai();
        if (trangThai == null || (!trangThai.equals("NHAP") && !trangThai.equals("CONG_KHAI"))) {
            trangThai = "NHAP";
        }

        DeThi deThi = new DeThi();
        deThi.setId(UUID.randomUUID().toString());
        deThi.setMaDeThi(taoMaDeThi());
        deThi.setTen(dto.getTenDeThi());
        deThi.setMonHoc(optMonHoc.get());
        deThi.setThoiGianPhut(dto.getThoiGianPhut());
        deThi.setMoTa(dto.getMoTa());
        deThi.setTrangThai(trangThai);
        deThi.setNguoiDung(optGiaoVien.get());
        deThi.setThoiGianTao(LocalDateTime.now());
        deThi.setTronCauHoi(false);
        deThi.setTronDapAn(false);

        DeThi saved = deThiRepository.save(deThi);
        return ApiResponse.success("Tạo đề thi thành công!", chuyenDoiDeThiDTO(saved));
    }

    // ================================================================
    // 3. CẬP NHẬT ĐỀ THI
    // ================================================================

    @Transactional
    public ApiResponse<DeThiListItemDTO> capNhatDeThi(String deThiId, CapNhatDeThiDTO dto, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        DeThi deThi = optDeThi.get();

        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền chỉnh sửa đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        Optional<MonHoc> optMonHoc = monHocRepository.findById(dto.getMonHocId());
        if (optMonHoc.isEmpty()) {
            return ApiResponse.error("Môn học không tồn tại!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        String trangThai = dto.getTrangThai();
        if (trangThai != null && !trangThai.equals("NHAP") && !trangThai.equals("CONG_KHAI")) {
            return ApiResponse.error("Trạng thái không hợp lệ (chỉ NHAP hoặc CONG_KHAI)!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        deThi.setTen(dto.getTenDeThi());
        deThi.setMonHoc(optMonHoc.get());
        deThi.setThoiGianPhut(dto.getThoiGianPhut());
        deThi.setMoTa(dto.getMoTa());
        if (trangThai != null) deThi.setTrangThai(trangThai);
        deThi.setThoiGianMo(dto.getThoiGianMo());
        deThi.setThoiGianDong(dto.getThoiGianDong());
        deThi.setSoLanThiToiDa(dto.getSoLanThiToiDa());
        if (dto.getTronCauHoi() != null) deThi.setTronCauHoi(dto.getTronCauHoi());
        if (dto.getTronDapAn() != null) deThi.setTronDapAn(dto.getTronDapAn());

        DeThi saved = deThiRepository.save(deThi);
        return ApiResponse.success("Cập nhật đề thi thành công!", chuyenDoiDeThiDTO(saved));
    }

    // ================================================================
    // 4. XÓA MỀM (SOFT DELETE)
    // ================================================================

    @Transactional
    public ApiResponse<Void> xoaMemDeThi(String deThiId, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi hoặc đề đã bị xóa!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        DeThi deThi = optDeThi.get();

        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền xóa đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        deThi.setDeletedAt(LocalDateTime.now());
        // Tự động đặt về NHAP khi xóa mềm để ngăn sinh viên truy cập
        deThi.setTrangThai("NHAP");
        deThiRepository.save(deThi);

        return ApiResponse.success("Đã chuyển đề thi vào thùng rác!");
    }

    // ================================================================
    // 5. KHÔI PHỤC ĐỀ THI (RESTORE)
    // ================================================================

    @Transactional
    public ApiResponse<Void> khoiPhucDeThi(String deThiId, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findById(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        DeThi deThi = optDeThi.get();

        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền khôi phục đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        if (deThi.getDeletedAt() == null) {
            return ApiResponse.error("Đề thi này chưa bị xóa!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        deThi.setDeletedAt(null);
        deThiRepository.save(deThi);
        return ApiResponse.success("Khôi phục đề thi thành công!");
    }

    // ================================================================
    // 6. XÓA HẲN (HARD DELETE) — chỉ khi chưa có bài làm
    // ================================================================

    @Transactional
    public ApiResponse<Void> xoaHanDeThi(String deThiId, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findById(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        DeThi deThi = optDeThi.get();

        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền xóa đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        // Ràng buộc: không được xóa hẳn nếu đã có sinh viên làm bài
        if (phienThiRepository.existsByDeThi(deThi)) {
            return ApiResponse.error(
                    "Không thể xóa hẳn đề thi này vì đã có sinh viên làm bài. Chỉ có thể xóa mềm.",
                    AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        // Xóa các liên kết câu hỏi trước, sau đó xóa đề thi
        deThiCauHoiRepository.deleteByDeThi(deThi);
        deThiRepository.delete(deThi);

        return ApiResponse.success("Đã xóa hẳn đề thi thành công!");
    }

    // ================================================================
    // 7. LẤY DANH SÁCH MÔN HỌC (cho dropdown)
    // ================================================================

    public ApiResponse<List<Map<String, String>>> layDanhSachMonHoc() {
        List<Map<String, String>> result = monHocRepository.findAll().stream()
                .map(mh -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("id", mh.getId());
                    map.put("ten", mh.getTen());
                    return map;
                })
                .toList();
        return ApiResponse.success("Lấy danh sách môn học thành công", result);
    }

    // ================================================================
    // HELPER: Chuyển DeThi entity → DeThiListItemDTO
    // ================================================================

    private DeThiListItemDTO chuyenDoiDeThiDTO(DeThi deThi) {
        DeThiListItemDTO dto = new DeThiListItemDTO();
        dto.setId(deThi.getId());
        dto.setMaDeThi(deThi.getMaDeThi());
        dto.setTenDeThi(deThi.getTen());
        dto.setMonHocId(deThi.getMonHoc() != null ? deThi.getMonHoc().getId() : null);
        dto.setTenMonHoc(deThi.getMonHoc() != null ? deThi.getMonHoc().getTen() : "N/A");
        dto.setThoiGianPhut(deThi.getThoiGianPhut());
        dto.setMoTa(deThi.getMoTa());
        dto.setTrangThai(deThi.getTrangThai() != null ? deThi.getTrangThai() : "NHAP");
        dto.setThoiGianMo(deThi.getThoiGianMo());
        dto.setThoiGianDong(deThi.getThoiGianDong());
        dto.setSoLanThiToiDa(deThi.getSoLanThiToiDa());
        dto.setTronCauHoi(deThi.getTronCauHoi());
        dto.setTronDapAn(deThi.getTronDapAn());
        dto.setDaBiXoa(deThi.getDeletedAt() != null);
        dto.setDeletedAt(deThi.getDeletedAt());
        dto.setThoiGianTao(deThi.getThoiGianTao());

        // Thống kê lượt thi
        dto.setSoLuotThi(phienThiRepository.findByDeThi(deThi).size());

        // Số câu hỏi trong đề
        dto.setSoCauHoi(deThiCauHoiRepository.countByDeThi(deThi));

        return dto;
    }

    /** Tạo mã đề thi dạng DE-XXXXXXXX (8 ký tự hex ngẫu nhiên) */
    private String taoMaDeThi() {
        return "DE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
