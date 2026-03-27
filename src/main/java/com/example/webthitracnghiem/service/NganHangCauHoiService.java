package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.model.*;
import com.example.webthitracnghiem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service — Quản lý Ngân hàng Câu hỏi (CRUD + Lọc + Phân loại).
 *
 * Mỗi câu hỏi thuộc một chủ đề (ChuDe), chủ đề thuộc một môn học (MonHoc).
 * Câu hỏi có thuộc tính: độ khó (DE/TRUNG_BINH/KHO), loại (TRAC_NGHIEM/DUNG_SAI/TU_LUAN).
 * Hỗ trợ lọc theo môn, chủ đề, độ khó và tìm kiếm theo từ khóa.
 */
@Service
public class NganHangCauHoiService {

    private final CauHoiRepository cauHoiRepository;
    private final ChuDeRepository chuDeRepository;
    private final MonHocRepository monHocRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public NganHangCauHoiService(CauHoiRepository cauHoiRepository,
                                  ChuDeRepository chuDeRepository,
                                  MonHocRepository monHocRepository,
                                  NguoiDungRepository nguoiDungRepository) {
        this.cauHoiRepository = cauHoiRepository;
        this.chuDeRepository = chuDeRepository;
        this.monHocRepository = monHocRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    // ================================================================
    // 1. LẤY DANH SÁCH CÂU HỎI (có lọc)
    // ================================================================

    /**
     * Lấy danh sách câu hỏi của giáo viên với các bộ lọc tùy chọn.
     *
     * @param nguoiDungId ID giáo viên
     * @param monHocId    Lọc theo môn học (null = tất cả)
     * @param chuDeId     Lọc theo chủ đề (null = tất cả)
     * @param doKho       Lọc theo độ khó: DE, TRUNG_BINH, KHO (null = tất cả)
     * @param keyword     Tìm kiếm trong nội dung câu hỏi (null = tất cả)
     */
    public ApiResponse<List<CauHoiListItemDTO>> layDanhSachCauHoi(
            String nguoiDungId, String monHocId, String chuDeId, String doKho, String keyword) {

        Optional<NguoiDung> optGiaoVien = nguoiDungRepository.findById(nguoiDungId);
        if (optGiaoVien.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        // Chuẩn hóa tham số — chuỗi rỗng xem như null để không lọc
        String monHocIdFilter = (monHocId != null && !monHocId.isBlank()) ? monHocId : null;
        String chuDeIdFilter  = (chuDeId  != null && !chuDeId.isBlank())  ? chuDeId  : null;
        String doKhoFilter    = (doKho    != null && !doKho.isBlank())    ? doKho    : null;
        String keywordFilter  = (keyword  != null && !keyword.isBlank())  ? keyword  : null;

        List<CauHoi> cauHoiList = cauHoiRepository.locCauHoi(
                optGiaoVien.get(), monHocIdFilter, chuDeIdFilter, doKhoFilter, keywordFilter);

        List<CauHoiListItemDTO> result = cauHoiList.stream()
                .map(this::chuyenDoiCauHoiDTO)
                .toList();

        return ApiResponse.success("Lấy danh sách câu hỏi thành công", result);
    }

    /**
     * Lấy chi tiết một câu hỏi theo ID.
     */
    public ApiResponse<CauHoiListItemDTO> layChiTietCauHoi(String cauHoiId, String nguoiDungId) {
        Optional<CauHoi> opt = cauHoiRepository.findById(cauHoiId);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy câu hỏi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        CauHoi cauHoi = opt.get();
        if (!cauHoi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền xem câu hỏi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        return ApiResponse.success("Lấy chi tiết câu hỏi thành công", chuyenDoiCauHoiDTO(cauHoi));
    }

    // ================================================================
    // 2. TẠO CÂU HỎI MỚI
    // ================================================================

    @Transactional
    public ApiResponse<CauHoiListItemDTO> taoCauHoi(TaoCauHoiDTO dto, String nguoiDungId) {
        Optional<NguoiDung> optGiaoVien = nguoiDungRepository.findById(nguoiDungId);
        if (optGiaoVien.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        Optional<ChuDe> optChuDe = chuDeRepository.findById(dto.getChuDeId());
        if (optChuDe.isEmpty()) {
            return ApiResponse.error("Chủ đề không tồn tại!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        // Kiểm tra độ khó hợp lệ
        if (!isDoKhoHopLe(dto.getDoKho())) {
            return ApiResponse.error("Độ khó không hợp lệ (chỉ DE, TRUNG_BINH, KHO)!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        // Với câu hỏi trắc nghiệm, bắt buộc phải có ít nhất 2 lựa chọn
        if ("TRAC_NGHIEM".equals(dto.getLoaiCauHoi())) {
            if (dto.getLuaChonA() == null || dto.getLuaChonA().isBlank()
                    || dto.getLuaChonB() == null || dto.getLuaChonB().isBlank()) {
                return ApiResponse.error("Câu hỏi trắc nghiệm cần ít nhất 2 lựa chọn (A và B)!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }

        CauHoi cauHoi = new CauHoi();
        cauHoi.setId(UUID.randomUUID().toString());
        cauHoi.setNoiDung(dto.getNoiDung());
        cauHoi.setChuDe(optChuDe.get());
        cauHoi.setLoaiCauHoi(dto.getLoaiCauHoi());
        cauHoi.setDoKho(dto.getDoKho());
        cauHoi.setDapAnDung(dto.getDapAnDung());
        cauHoi.setLuaChonA(dto.getLuaChonA());
        cauHoi.setLuaChonB(dto.getLuaChonB());
        cauHoi.setLuaChonC(dto.getLuaChonC());
        cauHoi.setLuaChonD(dto.getLuaChonD());
        cauHoi.setNguoiDung(optGiaoVien.get());

        CauHoi saved = cauHoiRepository.save(cauHoi);
        return ApiResponse.success("Thêm câu hỏi thành công!", chuyenDoiCauHoiDTO(saved));
    }

    // ================================================================
    // 3. CẬP NHẬT CÂU HỎI
    // ================================================================

    @Transactional
    public ApiResponse<CauHoiListItemDTO> capNhatCauHoi(String cauHoiId, CapNhatCauHoiDTO dto, String nguoiDungId) {
        Optional<CauHoi> opt = cauHoiRepository.findById(cauHoiId);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy câu hỏi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        CauHoi cauHoi = opt.get();
        if (!cauHoi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền chỉnh sửa câu hỏi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        Optional<ChuDe> optChuDe = chuDeRepository.findById(dto.getChuDeId());
        if (optChuDe.isEmpty()) {
            return ApiResponse.error("Chủ đề không tồn tại!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        if (!isDoKhoHopLe(dto.getDoKho())) {
            return ApiResponse.error("Độ khó không hợp lệ!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        cauHoi.setNoiDung(dto.getNoiDung());
        cauHoi.setChuDe(optChuDe.get());
        cauHoi.setLoaiCauHoi(dto.getLoaiCauHoi());
        cauHoi.setDoKho(dto.getDoKho());
        cauHoi.setDapAnDung(dto.getDapAnDung());
        cauHoi.setLuaChonA(dto.getLuaChonA());
        cauHoi.setLuaChonB(dto.getLuaChonB());
        cauHoi.setLuaChonC(dto.getLuaChonC());
        cauHoi.setLuaChonD(dto.getLuaChonD());

        CauHoi saved = cauHoiRepository.save(cauHoi);
        return ApiResponse.success("Cập nhật câu hỏi thành công!", chuyenDoiCauHoiDTO(saved));
    }

    // ================================================================
    // 4. XÓA CÂU HỎI
    // ================================================================

    @Transactional
    public ApiResponse<Void> xoaCauHoi(String cauHoiId, String nguoiDungId) {
        Optional<CauHoi> opt = cauHoiRepository.findById(cauHoiId);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy câu hỏi!", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        CauHoi cauHoi = opt.get();
        if (!cauHoi.getNguoiDung().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Bạn không có quyền xóa câu hỏi này!", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        // Cảnh báo nếu câu hỏi đang được dùng trong đề thi (vẫn cho xóa nhưng thông báo)
        long soDeThiSuDung = cauHoiRepository.demSoDeThiSuDung(cauHoiId);
        if (soDeThiSuDung > 0) {
            return ApiResponse.error(
                    "Không thể xóa câu hỏi này vì đang được dùng trong " + soDeThiSuDung + " đề thi!",
                    AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        cauHoiRepository.delete(cauHoi);
        return ApiResponse.success("Xóa câu hỏi thành công!");
    }

    // ================================================================
    // 5. QUẢN LÝ CHỦ ĐỀ (TOPIC)
    // ================================================================

    /** Lấy danh sách tất cả môn học (cho dropdown lọc) */
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

    /** Lấy danh sách chủ đề theo môn học */
    public ApiResponse<List<Map<String, String>>> layDanhSachChuDe(String monHocId) {
        Optional<MonHoc> optMonHoc = monHocRepository.findById(monHocId);
        if (optMonHoc.isEmpty()) {
            return ApiResponse.error("Môn học không tồn tại!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        List<Map<String, String>> result = chuDeRepository.findByMonHoc(optMonHoc.get()).stream()
                .map(cd -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("id", cd.getId());
                    map.put("ten", cd.getTen());
                    map.put("monHocId", cd.getMonHoc().getId());
                    return map;
                })
                .toList();

        return ApiResponse.success("Lấy danh sách chủ đề thành công", result);
    }

    /** Tạo chủ đề mới cho một môn học */
    @Transactional
    public ApiResponse<Map<String, String>> taoChuDe(String tenChuDe, String monHocId) {
        if (tenChuDe == null || tenChuDe.isBlank()) {
            return ApiResponse.error("Tên chủ đề không được để trống!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        Optional<MonHoc> optMonHoc = monHocRepository.findById(monHocId);
        if (optMonHoc.isEmpty()) {
            return ApiResponse.error("Môn học không tồn tại!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        MonHoc monHoc = optMonHoc.get();

        if (chuDeRepository.existsByTenAndMonHoc(tenChuDe.trim(), monHoc)) {
            return ApiResponse.error("Chủ đề '" + tenChuDe + "' đã tồn tại trong môn học này!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        ChuDe chuDe = new ChuDe();
        chuDe.setId(UUID.randomUUID().toString());
        chuDe.setTen(tenChuDe.trim());
        chuDe.setMonHoc(monHoc);
        ChuDe saved = chuDeRepository.save(chuDe);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("id", saved.getId());
        result.put("ten", saved.getTen());
        result.put("monHocId", monHocId);

        return ApiResponse.success("Tạo chủ đề thành công!", result);
    }

    // ================================================================
    // HELPER
    // ================================================================

    private CauHoiListItemDTO chuyenDoiCauHoiDTO(CauHoi cauHoi) {
        CauHoiListItemDTO dto = new CauHoiListItemDTO();
        dto.setId(cauHoi.getId());
        dto.setNoiDung(cauHoi.getNoiDung());
        dto.setLoaiCauHoi(cauHoi.getLoaiCauHoi());
        dto.setDoKho(cauHoi.getDoKho());
        dto.setDapAnDung(cauHoi.getDapAnDung());
        dto.setLuaChonA(cauHoi.getLuaChonA());
        dto.setLuaChonB(cauHoi.getLuaChonB());
        dto.setLuaChonC(cauHoi.getLuaChonC());
        dto.setLuaChonD(cauHoi.getLuaChonD());

        if (cauHoi.getChuDe() != null) {
            dto.setChuDeId(cauHoi.getChuDe().getId());
            dto.setTenChuDe(cauHoi.getChuDe().getTen());
            if (cauHoi.getChuDe().getMonHoc() != null) {
                dto.setMonHocId(cauHoi.getChuDe().getMonHoc().getId());
                dto.setTenMonHoc(cauHoi.getChuDe().getMonHoc().getTen());
            }
        }

        if (cauHoi.getNguoiDung() != null) {
            dto.setNguoiTaoId(cauHoi.getNguoiDung().getId());
            dto.setTenNguoiTao(cauHoi.getNguoiDung().getHoTen());
        }

        dto.setSoDeThiSuDung(cauHoiRepository.demSoDeThiSuDung(cauHoi.getId()));
        return dto;
    }

    private boolean isDoKhoHopLe(String doKho) {
        return "DE".equals(doKho) || "TRUNG_BINH".equals(doKho) || "KHO".equals(doKho);
    }
}
