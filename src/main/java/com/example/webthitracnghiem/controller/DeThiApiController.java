package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.DeThiService;
import com.example.webthitracnghiem.service.ImportDeThiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller — Quản lý Đề Thi (CRUD + Soft/Hard Delete + Import từ file).
 *
 * Tất cả endpoints yêu cầu JWT hợp lệ với vai trò GIAO_VIEN.
 * Token được kiểm tra bởi JwtAuthFilter và gắn vào request attributes.
 *
 * Base URL: /api/giao-vien/de-thi
 */
@RestController
@RequestMapping("/api/giao-vien/de-thi")
public class DeThiApiController {

    private final DeThiService deThiService;
    private final ImportDeThiService importDeThiService;

    public DeThiApiController(DeThiService deThiService, ImportDeThiService importDeThiService) {
        this.deThiService = deThiService;
        this.importDeThiService = importDeThiService;
    }

    // ================================================================
    // GET /api/giao-vien/de-thi — Danh sách đề thi (chưa xóa)
    // ================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeThiListItemDTO>>> layDanhSach(HttpServletRequest request) {
        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<List<DeThiListItemDTO>> res = deThiService.layDanhSachDeThi(userId);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.status(404).body(res);
    }

    // ================================================================
    // GET /api/giao-vien/de-thi/thung-rac — Đề thi đã xóa mềm
    // ================================================================

    @GetMapping("/thung-rac")
    public ResponseEntity<ApiResponse<List<DeThiListItemDTO>>> layThungRac(HttpServletRequest request) {
        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<List<DeThiListItemDTO>> res = deThiService.layDeThiDaXoa(userId);
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // GET /api/giao-vien/de-thi/mon-hoc — Danh sách môn học (dropdown)
    // ================================================================

    @GetMapping("/mon-hoc")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> layDanhSachMonHoc(HttpServletRequest request) {
        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        return ResponseEntity.ok(deThiService.layDanhSachMonHoc());
    }

    // ================================================================
    // GET /api/giao-vien/de-thi/{id} — Chi tiết đề thi
    // ================================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeThiListItemDTO>> layChiTiet(
            @PathVariable("id") String deThiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<DeThiListItemDTO> res = deThiService.layChiTietDeThi(deThiId, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // POST /api/giao-vien/de-thi — Tạo đề thi mới
    // ================================================================

    @PostMapping
    public ResponseEntity<ApiResponse<DeThiListItemDTO>> taoDeThi(
            @Valid @RequestBody TaoDeThiDTO dto,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<DeThiListItemDTO> res = deThiService.taoDeThi(dto, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // ================================================================
    // PUT /api/giao-vien/de-thi/{id} — Cập nhật đề thi
    // ================================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeThiListItemDTO>> capNhatDeThi(
            @PathVariable("id") String deThiId,
            @Valid @RequestBody CapNhatDeThiDTO dto,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<DeThiListItemDTO> res = deThiService.capNhatDeThi(deThiId, dto, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // DELETE /api/giao-vien/de-thi/{id}/soft — Xóa mềm
    // ================================================================

    @DeleteMapping("/{id}/soft")
    public ResponseEntity<ApiResponse<Void>> xoaMem(
            @PathVariable("id") String deThiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoiVoid401();

        ApiResponse<Void> res = deThiService.xoaMemDeThi(deThiId, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // DELETE /api/giao-vien/de-thi/{id}/hard — Xóa hẳn (có ràng buộc)
    // ================================================================

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> xoaHan(
            @PathVariable("id") String deThiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoiVoid401();

        ApiResponse<Void> res = deThiService.xoaHanDeThi(deThiId, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // PATCH /api/giao-vien/de-thi/{id}/restore — Khôi phục từ thùng rác
    // ================================================================

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> khoiPhuc(
            @PathVariable("id") String deThiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoiVoid401();

        ApiResponse<Void> res = deThiService.khoiPhucDeThi(deThiId, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // GET /api/giao-vien/de-thi/{id}/cau-hoi — Câu hỏi trong đề
    // ================================================================

    @GetMapping("/{id}/cau-hoi")
    public ResponseEntity<ApiResponse<List<DeThiCauHoiDTO>>> layCauHoiTrongDe(
            @PathVariable("id") String deThiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<List<DeThiCauHoiDTO>> res = deThiService.layCauHoiTrongDe(deThiId, userId);
        if (!res.isSuccess()) return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // GET /api/giao-vien/de-thi/{id}/cau-hoi/ngan-hang — Ngân hàng chưa có trong đề
    // ================================================================

    @GetMapping("/{id}/cau-hoi/ngan-hang")
    public ResponseEntity<ApiResponse<List<CauHoiListItemDTO>>> layNganHangChoThem(
            @PathVariable("id") String deThiId,
            @RequestParam(value = "monHocId", required = false) String monHocId,
            @RequestParam(value = "chuDeId",  required = false) String chuDeId,
            @RequestParam(value = "doKho",    required = false) String doKho,
            @RequestParam(value = "keyword",  required = false) String keyword,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<List<CauHoiListItemDTO>> res =
                deThiService.layNganHangChoThem(deThiId, userId, monHocId, chuDeId, doKho, keyword);
        if (!res.isSuccess()) return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // POST /api/giao-vien/de-thi/{id}/cau-hoi — Thêm câu hỏi vào đề
    // ================================================================

    @PostMapping("/{id}/cau-hoi")
    public ResponseEntity<ApiResponse<Map<String, Object>>> themCauHoi(
            @PathVariable("id") String deThiId,
            @Valid @RequestBody ThemCauHoiVaoDeDTO dto,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<Map<String, Object>> res =
                deThiService.themCauHoiVaoDe(deThiId, dto.getCauHoiIds(), userId);
        if (!res.isSuccess()) return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // DELETE /api/giao-vien/de-thi/{id}/cau-hoi/{cauHoiId} — Xóa câu hỏi khỏi đề
    // ================================================================

    @DeleteMapping("/{id}/cau-hoi/{cauHoiId}")
    public ResponseEntity<ApiResponse<Void>> xoaCauHoiKhoiDe(
            @PathVariable("id")       String deThiId,
            @PathVariable("cauHoiId") String cauHoiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoiVoid401();

        ApiResponse<Void> res = deThiService.xoaCauHoiKhoiDe(deThiId, cauHoiId, userId);
        if (!res.isSuccess()) return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // PUT /api/giao-vien/de-thi/{id}/cau-hoi/thu-tu — Sắp xếp thứ tự
    // ================================================================

    @PutMapping("/{id}/cau-hoi/thu-tu")
    public ResponseEntity<ApiResponse<Void>> capNhatThuTu(
            @PathVariable("id") String deThiId,
            @RequestBody List<String> cauHoiIdsOrdered,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoiVoid401();

        ApiResponse<Void> res = deThiService.capNhatThuTu(deThiId, cauHoiIdsOrdered, userId);
        if (!res.isSuccess()) return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // GET /api/giao-vien/de-thi/{id}/cau-hoi/van-ban-tho — Văn bản thô (trang split-view)
    // ================================================================

    @GetMapping("/{id}/cau-hoi/van-ban-tho")
    public ResponseEntity<ApiResponse<DeThiVanBanCauHoiDTO>> layVanBanThoCauHoi(
            @PathVariable("id") String deThiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<DeThiVanBanCauHoiDTO> res = deThiService.layVanBanThoCauHoiTrongDe(deThiId, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // PUT /api/giao-vien/de-thi/{id}/cau-hoi/van-ban-tho — Lưu văn bản thô
    // ================================================================

    @PutMapping("/{id}/cau-hoi/van-ban-tho")
    public ResponseEntity<ApiResponse<Void>> luuVanBanThoCauHoi(
            @PathVariable("id") String deThiId,
            @RequestBody LuuVanBanCauHoiRequestDTO body,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoiVoid401();

        ApiResponse<Void> res = deThiService.luuVanBanThoCauHoiTrongDe(deThiId, userId, body);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // POST /api/giao-vien/de-thi/import — Import từ file PDF/DOCX
    // ================================================================

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<ImportKetQuaDTO>> importFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Phiên đăng nhập không hợp lệ!", AuthService.ERR_HE_THONG));

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng chọn file để import!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE));
        }

        String tenFile = file.getOriginalFilename();
        ImportKetQuaDTO ketQua;

        if (tenFile != null && tenFile.toLowerCase().endsWith(".pdf")) {
            ketQua = importDeThiService.parsePDF(file);
        } else if (tenFile != null && (tenFile.toLowerCase().endsWith(".docx") || tenFile.toLowerCase().endsWith(".doc"))) {
            ketQua = importDeThiService.parseDOCX(file);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chỉ hỗ trợ file PDF và DOCX!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE));
        }

        if (!ketQua.isSuccess()) {
            return ResponseEntity.status(422)
                    .body(ApiResponse.error(ketQua.getMessage(), AuthService.ERR_DU_LIEU_KHONG_HOP_LE));
        }

        return ResponseEntity.ok(ApiResponse.success(ketQua.getMessage(), ketQua));
    }

    // ================================================================
    // HELPER METHODS
    // ================================================================

    /** Lấy userId từ JWT attributes (chỉ cho GIAO_VIEN) */
    private static String layUserIdTuJwt(HttpServletRequest request) {
        Object id = request.getAttribute("jwtUserId");
        Object role = request.getAttribute("jwtVaiTro");
        if (id == null || role == null) return null;
        if (!AuthService.ROLE_GIAO_VIEN.equals(role.toString())) return null;
        return id.toString();
    }

    private <T> ResponseEntity<ApiResponse<T>> traVeLoi401() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
    }

    private ResponseEntity<ApiResponse<Void>> traVeLoiVoid401() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
    }

    private HttpStatus mapStatusCode(int errorCode) {
        return switch (errorCode) {
            case AuthService.ERR_VAI_TRO_KHONG_HOP_LE -> HttpStatus.FORBIDDEN;
            case AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
