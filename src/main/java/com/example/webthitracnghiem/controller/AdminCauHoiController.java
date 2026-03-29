package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.service.AdminCauHoiService;
import com.example.webthitracnghiem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API — Admin quản lý câu hỏi toàn hệ thống.
 * Master-detail: cấp 1 danh sách GV → cấp 2 câu hỏi GV → chi tiết câu hỏi.
 */
@RestController
@RequestMapping("/api/admin/cau-hoi")
public class AdminCauHoiController {

    private final AdminCauHoiService adminCauHoiService;

    public AdminCauHoiController(AdminCauHoiService adminCauHoiService) {
        this.adminCauHoiService = adminCauHoiService;
    }

    /**
     * Cấp 1: Danh sách giảng viên + thống kê câu hỏi.
     * GET /api/admin/cau-hoi/giao-vien
     */
    @GetMapping("/giao-vien")
    public ResponseEntity<ApiResponse<List<AdminGiaoVienCauHoiSummaryDTO>>> layDanhSachGiaoVien(
            HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        return ResponseEntity.ok(adminCauHoiService.layDanhSachGiaoVienCauHoi());
    }

    /**
     * Cấp 2: Câu hỏi của 1 giảng viên (có lọc).
     * GET /api/admin/cau-hoi/giao-vien/{gvId}?monHocId=&chuDeId=&doKho=&keyword=
     */
    @GetMapping("/giao-vien/{gvId}")
    public ResponseEntity<ApiResponse<List<AdminCauHoiItemDTO>>> layCauHoiCuaGV(
            @PathVariable String gvId,
            @RequestParam(value = "monHocId", required = false) String monHocId,
            @RequestParam(value = "chuDeId", required = false) String chuDeId,
            @RequestParam(value = "doKho", required = false) String doKho,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        return ResponseEntity.ok(adminCauHoiService.layCauHoiTheoGiaoVien(gvId, monHocId, chuDeId, doKho, keyword));
    }

    /**
     * Chi tiết 1 câu hỏi.
     * GET /api/admin/cau-hoi/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminCauHoiItemDTO>> layChiTiet(
            @PathVariable String id,
            HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        ApiResponse<AdminCauHoiItemDTO> res = adminCauHoiService.layChiTietCauHoi(id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Xóa câu hỏi.
     * DELETE /api/admin/cau-hoi/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaCauHoi(
            @PathVariable String id,
            HttpServletRequest request) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<Void> res = adminCauHoiService.xoaCauHoi(id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Danh sách môn học (dropdown).
     * GET /api/admin/cau-hoi/mon-hoc
     */
    @GetMapping("/mon-hoc")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> layMonHoc(HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        return ResponseEntity.ok(adminCauHoiService.layDanhSachMonHoc());
    }

    /**
     * Danh sách chủ đề theo môn.
     * GET /api/admin/cau-hoi/chu-de?monHocId=xxx
     */
    @GetMapping("/chu-de")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> layChuDe(
            @RequestParam("monHocId") String monHocId,
            HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        return ResponseEntity.ok(adminCauHoiService.layDanhSachChuDe(monHocId));
    }

    // ================ HELPER ================

    private static boolean laAdmin(HttpServletRequest request) {
        Object role = request.getAttribute("jwtVaiTro");
        Object uid = request.getAttribute("jwtUserId");
        return uid != null && AuthService.ROLE_ADMIN.equals(String.valueOf(role));
    }

    private <T> ResponseEntity<ApiResponse<T>> traVeLoi401() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
    }
}
