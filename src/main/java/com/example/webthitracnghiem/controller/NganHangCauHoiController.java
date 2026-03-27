package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.NganHangCauHoiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller — Quản lý Ngân hàng Câu hỏi (CRUD + Lọc + Phân loại).
 *
 * Tất cả endpoints yêu cầu JWT hợp lệ với vai trò GIAO_VIEN.
 *
 * Base URL: /api/giao-vien/ngan-hang-cau-hoi
 */
@RestController
@RequestMapping("/api/giao-vien/ngan-hang-cau-hoi")
public class NganHangCauHoiController {

    private final NganHangCauHoiService nganHangCauHoiService;

    public NganHangCauHoiController(NganHangCauHoiService nganHangCauHoiService) {
        this.nganHangCauHoiService = nganHangCauHoiService;
    }

    // ================================================================
    // GET /api/giao-vien/ngan-hang-cau-hoi — Danh sách câu hỏi (có lọc)
    //
    // Query params (tùy chọn):
    //   ?monHocId=xxx    lọc theo môn học
    //   &chuDeId=xxx     lọc theo chủ đề
    //   &doKho=DE        lọc theo độ khó (DE, TRUNG_BINH, KHO)
    //   &keyword=xxx     tìm kiếm theo từ khóa trong nội dung
    // ================================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<CauHoiListItemDTO>>> layDanhSach(
            HttpServletRequest request,
            @RequestParam(value = "monHocId", required = false) String monHocId,
            @RequestParam(value = "chuDeId",  required = false) String chuDeId,
            @RequestParam(value = "doKho",    required = false) String doKho,
            @RequestParam(value = "keyword",  required = false) String keyword) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<List<CauHoiListItemDTO>> res =
                nganHangCauHoiService.layDanhSachCauHoi(userId, monHocId, chuDeId, doKho, keyword);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.status(404).body(res);
    }

    // ================================================================
    // GET /api/giao-vien/ngan-hang-cau-hoi/{id} — Chi tiết câu hỏi
    // ================================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CauHoiListItemDTO>> layChiTiet(
            @PathVariable("id") String cauHoiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<CauHoiListItemDTO> res = nganHangCauHoiService.layChiTietCauHoi(cauHoiId, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // POST /api/giao-vien/ngan-hang-cau-hoi — Thêm câu hỏi mới
    // ================================================================

    @PostMapping
    public ResponseEntity<ApiResponse<CauHoiListItemDTO>> taoCauHoi(
            @Valid @RequestBody TaoCauHoiDTO dto,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<CauHoiListItemDTO> res = nganHangCauHoiService.taoCauHoi(dto, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // ================================================================
    // PUT /api/giao-vien/ngan-hang-cau-hoi/{id} — Cập nhật câu hỏi
    // ================================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CauHoiListItemDTO>> capNhatCauHoi(
            @PathVariable("id") String cauHoiId,
            @Valid @RequestBody CapNhatCauHoiDTO dto,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<CauHoiListItemDTO> res = nganHangCauHoiService.capNhatCauHoi(cauHoiId, dto, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // DELETE /api/giao-vien/ngan-hang-cau-hoi/{id} — Xóa câu hỏi
    // ================================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaCauHoi(
            @PathVariable("id") String cauHoiId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Phiên đăng nhập không hợp lệ!", AuthService.ERR_HE_THONG));

        ApiResponse<Void> res = nganHangCauHoiService.xoaCauHoi(cauHoiId, userId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapStatusCode(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    // ================================================================
    // GET /api/giao-vien/ngan-hang-cau-hoi/mon-hoc — Danh sách môn học
    // ================================================================

    @GetMapping("/mon-hoc")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> layDanhSachMonHoc(
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        return ResponseEntity.ok(nganHangCauHoiService.layDanhSachMonHoc());
    }

    // ================================================================
    // GET /api/giao-vien/ngan-hang-cau-hoi/chu-de?monHocId=xxx — Chủ đề theo môn
    // ================================================================

    @GetMapping("/chu-de")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> layDanhSachChuDe(
            @RequestParam("monHocId") String monHocId,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return traVeLoi401();

        ApiResponse<List<Map<String, String>>> res = nganHangCauHoiService.layDanhSachChuDe(monHocId);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    // ================================================================
    // POST /api/giao-vien/ngan-hang-cau-hoi/chu-de — Tạo chủ đề mới
    // ================================================================

    @PostMapping("/chu-de")
    public ResponseEntity<ApiResponse<Map<String, String>>> taoChuDe(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String userId = layUserIdTuJwt(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Phiên đăng nhập không hợp lệ!", AuthService.ERR_HE_THONG));

        String ten = body.get("ten");
        String monHocId = body.get("monHocId");

        if (ten == null || ten.isBlank() || monHocId == null || monHocId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Tên chủ đề và môn học không được để trống!", AuthService.ERR_DU_LIEU_KHONG_HOP_LE));
        }

        ApiResponse<Map<String, String>> res = nganHangCauHoiService.taoChuDe(ten, monHocId);
        if (!res.isSuccess()) {
            return ResponseEntity.badRequest().body(res);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // ================================================================
    // HELPER METHODS
    // ================================================================

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

    private HttpStatus mapStatusCode(int errorCode) {
        return switch (errorCode) {
            case AuthService.ERR_VAI_TRO_KHONG_HOP_LE -> HttpStatus.FORBIDDEN;
            case AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
