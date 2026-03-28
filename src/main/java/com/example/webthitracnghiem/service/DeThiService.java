package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.model.*;
import com.example.webthitracnghiem.repository.*;
import com.example.webthitracnghiem.repository.CauHoiRepository;
import com.example.webthitracnghiem.util.DeThiCauHoiVanBanCodec;
import com.example.webthitracnghiem.util.DeThiCauHoiVanBanCodec.ParsedMcq;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final CauHoiRepository cauHoiRepository;

    public DeThiService(DeThiRepository deThiRepository,
                        MonHocRepository monHocRepository,
                        NguoiDungRepository nguoiDungRepository,
                        PhienThiRepository phienThiRepository,
                        DeThiCauHoiRepository deThiCauHoiRepository,
                        CauHoiRepository cauHoiRepository) {
        this.deThiRepository = deThiRepository;
        this.monHocRepository = monHocRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.phienThiRepository = phienThiRepository;
        this.deThiCauHoiRepository = deThiCauHoiRepository;
        this.cauHoiRepository = cauHoiRepository;
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
        deThi.setThoiGianMo(dto.getThoiGianMo());
        deThi.setThoiGianDong(dto.getThoiGianDong());
        deThi.setSoLanThiToiDa(dto.getSoLanThiToiDa());
        deThi.setTronCauHoi(false);
        deThi.setTronDapAn(false);
        deThi.setThangDiemToiDa(BigDecimal.TEN);

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
        // Không còn UI trộn — luôn đặt false
        deThi.setTronCauHoi(false);
        deThi.setTronDapAn(false);

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

    // ================================================================
    // 8. QUẢN LÝ CÂU HỎI TRONG ĐỀ THI
    // ================================================================

    /**
     * Lấy danh sách câu hỏi đang có trong đề thi (sắp xếp theo thứ tự).
     */
    public ApiResponse<List<DeThiCauHoiDTO>> layCauHoiTrongDe(String deThiId, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = optDeThi.get();
        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền truy cập đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        List<DeThiCauHoiDTO> result = deThiCauHoiRepository
                .findByDeThiOrderByThuTuAsc(deThi)
                .stream()
                .map(this::chuyenDoiDeThiCauHoiDTO)
                .toList();

        return ApiResponse.success("Lấy danh sách câu hỏi thành công", result);
    }

    /**
     * Thêm nhiều câu hỏi từ ngân hàng vào đề thi.
     * Bỏ qua câu hỏi đã có trong đề (không báo lỗi, chỉ thêm câu mới).
     */
    @Transactional
    public ApiResponse<Map<String, Object>> themCauHoiVaoDe(String deThiId,
                                                             List<String> cauHoiIds,
                                                             String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = optDeThi.get();
        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền chỉnh sửa đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        int thuTuHienTai = deThiCauHoiRepository.layThuTuLonNhat(deThi);
        int daThemCount  = 0;
        int trungCount   = 0;

        for (String cauHoiId : cauHoiIds) {
            Optional<CauHoi> optCauHoi = cauHoiRepository.findById(cauHoiId);
            if (optCauHoi.isEmpty()) continue;

            CauHoi cauHoi = optCauHoi.get();

            // Bỏ qua nếu câu hỏi đã có trong đề
            if (deThiCauHoiRepository.existsByDeThiAndCauHoi(deThi, cauHoi)) {
                trungCount++;
                continue;
            }

            DeThiCauHoi link = new DeThiCauHoi();
            link.setId(UUID.randomUUID().toString());
            link.setDeThi(deThi);
            link.setCauHoi(cauHoi);
            link.setThuTu(++thuTuHienTai);
            deThiCauHoiRepository.save(link);
            daThemCount++;
        }

        String msg = "Đã thêm " + daThemCount + " câu hỏi vào đề thi.";
        if (trungCount > 0) msg += " (" + trungCount + " câu đã tồn tại, bỏ qua)";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("daThemCount", daThemCount);
        data.put("trungCount", trungCount);
        data.put("tongCauHoi", deThiCauHoiRepository.countByDeThi(deThi));

        return ApiResponse.success(msg, data);
    }

    /**
     * Xóa một câu hỏi khỏi đề thi và tái đánh số thứ tự.
     */
    @Transactional
    public ApiResponse<Void> xoaCauHoiKhoiDe(String deThiId, String cauHoiId, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = optDeThi.get();
        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền chỉnh sửa đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        Optional<CauHoi> optCauHoi = cauHoiRepository.findById(cauHoiId);
        if (optCauHoi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy câu hỏi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        deThiCauHoiRepository.deleteByDeThiAndCauHoi(deThi, optCauHoi.get());

        // Tái đánh số thứ tự để không bị lỗ khoảng
        List<DeThiCauHoi> remaining = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(deThi);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setThuTu(i + 1);
            deThiCauHoiRepository.save(remaining.get(i));
        }

        return ApiResponse.success("Đã xóa câu hỏi khỏi đề thi!");
    }

    /**
     * Cập nhật thứ tự câu hỏi trong đề thi.
     * Nhận vào danh sách ID câu hỏi theo thứ tự mong muốn.
     */
    @Transactional
    public ApiResponse<Void> capNhatThuTu(String deThiId, List<String> cauHoiIdsOrdered, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = optDeThi.get();
        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền chỉnh sửa đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        List<DeThiCauHoi> allLinks = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(deThi);

        // Tạo map cauHoiId → link để tra cứu nhanh
        Map<String, DeThiCauHoi> linkMap = new LinkedHashMap<>();
        for (DeThiCauHoi link : allLinks) {
            linkMap.put(link.getCauHoi().getId(), link);
        }

        // Cập nhật thứ tự theo danh sách nhận vào
        for (int i = 0; i < cauHoiIdsOrdered.size(); i++) {
            DeThiCauHoi link = linkMap.get(cauHoiIdsOrdered.get(i));
            if (link != null) {
                link.setThuTu(i + 1);
                deThiCauHoiRepository.save(link);
            }
        }

        return ApiResponse.success("Đã cập nhật thứ tự câu hỏi!");
    }

    /**
     * Văn bản thô các câu trắc nghiệm trong đề (trang chỉnh sửa split-view).
     */
    @Transactional(readOnly = true)
    public ApiResponse<DeThiVanBanCauHoiDTO> layVanBanThoCauHoiTrongDe(String deThiId, String nguoiDungId) {
        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = optDeThi.get();
        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền truy cập đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        List<DeThiCauHoi> links = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(deThi);
        for (DeThiCauHoi link : links) {
            CauHoi c = link.getCauHoi();
            if (c.getLoaiCauHoi() == null || !"TRAC_NGHIEM".equals(c.getLoaiCauHoi())) {
                return ApiResponse.error(
                        "Đề có câu không phải trắc nghiệm 4 phương án — không dùng được trình sửa văn bản thô. Chỉnh trong Ngân hàng câu hỏi.",
                        AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }

        List<DeThiCauHoiDTO> dtos = links.stream().map(this::chuyenDoiDeThiCauHoiDTO).toList();
        String vanBan = DeThiCauHoiVanBanCodec.serialize(dtos);

        DeThiVanBanCauHoiDTO out = new DeThiVanBanCauHoiDTO();
        out.setDeThiId(deThi.getId());
        out.setTenDeThi(deThi.getTen());
        out.setMaDeThi(deThi.getMaDeThi());
        out.setTrangThai(deThi.getTrangThai() != null ? deThi.getTrangThai() : "NHAP");
        out.setTenMonHoc(deThi.getMonHoc() != null ? deThi.getMonHoc().getTen() : "");
        int soCau = links.size();
        out.setSoCau(soCau);
        BigDecimal thang = layThangDiemToiDaHieuLuc(deThi);
        out.setThangDiemToiDa(thang);
        if (soCau > 0) {
            out.setDiemMoiCau(tinhDiemMoiCau(thang, soCau));
        } else {
            out.setDiemMoiCau(null);
        }
        out.setVanBan(vanBan);
        return ApiResponse.success("OK", out);
    }

    /**
     * Lưu nội dung câu hỏi từ văn bản thô (số câu và thứ tự không đổi).
     */
    @Transactional
    public ApiResponse<Void> luuVanBanThoCauHoiTrongDe(String deThiId, String nguoiDungId, LuuVanBanCauHoiRequestDTO body) {
        String vanBan = body != null ? body.getVanBan() : null;
        BigDecimal thangCapNhat = body != null ? body.getThangDiemToiDa() : null;

        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = optDeThi.get();
        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền chỉnh sửa đề thi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        if (thangCapNhat != null) {
            if (thangCapNhat.compareTo(new BigDecimal("0.01")) < 0
                    || thangCapNhat.compareTo(new BigDecimal("1000")) > 0) {
                return ApiResponse.error("Thang điểm phải từ 0,01 đến 1000.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }

        List<DeThiCauHoi> links = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(deThi);
        for (DeThiCauHoi link : links) {
            CauHoi c = link.getCauHoi();
            if (c.getLoaiCauHoi() == null || !"TRAC_NGHIEM".equals(c.getLoaiCauHoi())) {
                return ApiResponse.error(
                        "Đề có câu không phải trắc nghiệm — không lưu được qua văn bản thô.",
                        AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }

        String raw = vanBan == null ? "" : vanBan;
        if (raw.isBlank()) {
            if (links.isEmpty()) {
                return ApiResponse.success("OK");
            }
            return ApiResponse.error(
                    "Văn bản trống nhưng đề vẫn có " + links.size() + " câu — không thể xóa câu tại đây.",
                    AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        List<ParsedMcq> parsed = new ArrayList<>();
        String err = DeThiCauHoiVanBanCodec.parse(raw, parsed);
        if (err != null) {
            return ApiResponse.error(err, AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (parsed.size() != links.size()) {
            return ApiResponse.error(
                    "Số câu trong văn bản (" + parsed.size() + ") phải trùng số câu trong đề (" + links.size() + ").",
                    AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        for (int i = 0; i < links.size(); i++) {
            CauHoi ch = links.get(i).getCauHoi();
            if (!ch.getNguoiDung().getId().equals(nguoiDungId)) {
                return ApiResponse.error("Có câu hỏi không thuộc tài khoản của bạn!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
            }
            ParsedMcq p = parsed.get(i);
            ch.setNoiDung(p.getNoiDung());
            ch.setLuaChonA(chuoiRongThanhNull(p.getLuaChonA()));
            ch.setLuaChonB(chuoiRongThanhNull(p.getLuaChonB()));
            ch.setLuaChonC(chuoiRongThanhNull(p.getLuaChonC()));
            ch.setLuaChonD(chuoiRongThanhNull(p.getLuaChonD()));
            ch.setDapAnDung(p.getDapAnDung());
            cauHoiRepository.save(ch);
        }
        if (thangCapNhat != null) {
            deThi.setThangDiemToiDa(thangCapNhat.setScale(6, RoundingMode.HALF_UP));
            deThiRepository.save(deThi);
        }
        return ApiResponse.success("Đã lưu nội dung câu hỏi.");
    }

    /** Thang điểm lưu trên đề; null hoặc không hợp lệ → mặc định 10. */
    private static BigDecimal layThangDiemToiDaHieuLuc(DeThi deThi) {
        BigDecimal t = deThi.getThangDiemToiDa();
        if (t == null || t.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.TEN;
        }
        return t;
    }

    /** Điểm mỗi câu khi chia đều thang điểm cho số câu. */
    private static BigDecimal tinhDiemMoiCau(BigDecimal thangDiemToiDa, int soCau) {
        if (soCau <= 0 || thangDiemToiDa == null) {
            return null;
        }
        return thangDiemToiDa.divide(BigDecimal.valueOf(soCau), 6, RoundingMode.HALF_UP);
    }

    private static String chuoiRongThanhNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }

    /**
     * Lấy danh sách câu hỏi từ ngân hàng (của giáo viên) chưa có trong đề thi,
     * có thể lọc theo môn học, chủ đề, độ khó, từ khóa.
     */
    public ApiResponse<List<CauHoiListItemDTO>> layNganHangChoThem(
            String deThiId, String nguoiDungId,
            String monHocId, String chuDeId, String doKho, String keyword) {

        Optional<DeThi> optDeThi = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (optDeThi.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = optDeThi.get();
        if (!deThi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền truy cập!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        Optional<NguoiDung> optGV = nguoiDungRepository.findById(nguoiDungId);
        if (optGV.isEmpty()) return ApiResponse.error("Không tìm thấy người dùng!", 1);

        // ID các câu hỏi đã có trong đề (để loại trừ)
        java.util.Set<String> daTrongDe = deThiCauHoiRepository
                .findByDeThiOrderByThuTuAsc(deThi)
                .stream()
                .map(dc -> dc.getCauHoi().getId())
                .collect(java.util.stream.Collectors.toSet());

        // Chuẩn hóa filter
        String mhFilter  = (monHocId != null && !monHocId.isBlank())  ? monHocId  : null;
        String cdFilter  = (chuDeId  != null && !chuDeId.isBlank())   ? chuDeId   : null;
        String dkFilter  = (doKho    != null && !doKho.isBlank())     ? doKho     : null;
        String kwFilter  = (keyword  != null && !keyword.isBlank())   ? keyword   : null;

        List<CauHoiListItemDTO> result = cauHoiRepository
                .locCauHoi(optGV.get(), mhFilter, cdFilter, dkFilter, kwFilter)
                .stream()
                .filter(c -> !daTrongDe.contains(c.getId()))
                .map(c -> {
                    CauHoiListItemDTO dto = new CauHoiListItemDTO();
                    dto.setId(c.getId());
                    dto.setNoiDung(c.getNoiDung());
                    dto.setLoaiCauHoi(c.getLoaiCauHoi());
                    dto.setDoKho(c.getDoKho());
                    dto.setDapAnDung(c.getDapAnDung());
                    dto.setLuaChonA(c.getLuaChonA());
                    dto.setLuaChonB(c.getLuaChonB());
                    dto.setLuaChonC(c.getLuaChonC());
                    dto.setLuaChonD(c.getLuaChonD());
                    if (c.getChuDe() != null) {
                        dto.setChuDeId(c.getChuDe().getId());
                        dto.setTenChuDe(c.getChuDe().getTen());
                        if (c.getChuDe().getMonHoc() != null) {
                            dto.setMonHocId(c.getChuDe().getMonHoc().getId());
                            dto.setTenMonHoc(c.getChuDe().getMonHoc().getTen());
                        }
                    }
                    return dto;
                })
                .toList();

        return ApiResponse.success("Lấy ngân hàng câu hỏi thành công", result);
    }

    // ── Helper: chuyển DeThiCauHoi → DTO ──────────────────────────
    private DeThiCauHoiDTO chuyenDoiDeThiCauHoiDTO(DeThiCauHoi link) {
        DeThiCauHoiDTO dto = new DeThiCauHoiDTO();
        dto.setLinkId(link.getId());
        dto.setThuTu(link.getThuTu() != null ? link.getThuTu() : 0);

        CauHoi c = link.getCauHoi();
        dto.setCauHoiId(c.getId());
        dto.setNoiDung(c.getNoiDung());
        dto.setLoaiCauHoi(c.getLoaiCauHoi());
        dto.setDoKho(c.getDoKho());
        dto.setDapAnDung(c.getDapAnDung());
        dto.setLuaChonA(c.getLuaChonA());
        dto.setLuaChonB(c.getLuaChonB());
        dto.setLuaChonC(c.getLuaChonC());
        dto.setLuaChonD(c.getLuaChonD());

        if (c.getChuDe() != null) {
            dto.setTenChuDe(c.getChuDe().getTen());
            if (c.getChuDe().getMonHoc() != null) {
                dto.setTenMonHoc(c.getChuDe().getMonHoc().getTen());
            }
        }
        return dto;
    }
}
