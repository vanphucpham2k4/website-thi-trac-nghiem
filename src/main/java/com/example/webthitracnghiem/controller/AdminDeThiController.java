package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.service.AdminDeThiService;
import com.example.webthitracnghiem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API — Admin quản lý đề thi toàn hệ thống.
 * Master-detail: cấp 1 danh sách GV → cấp 2 đề thi GV → chi tiết đề.
 */
@RestController
@RequestMapping("/api/admin/de-thi")
public class AdminDeThiController {

    private final AdminDeThiService adminDeThiService;

    public AdminDeThiController(AdminDeThiService adminDeThiService) {
        this.adminDeThiService = adminDeThiService;
    }

    /**
     * Cấp 1: Danh sách giảng viên + thống kê đề thi.
     * GET /api/admin/de-thi/giao-vien
     */
    @GetMapping("/giao-vien")
    public ResponseEntity<ApiResponse<List<AdminGiaoVienDeThiSummaryDTO>>> layDanhSachGiaoVien(
            HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        return ResponseEntity.ok(adminDeThiService.layDanhSachGiaoVienDeThi());
    }

    /**
     * Cấp 2: Đề thi của 1 giảng viên (có lọc).
     * GET /api/admin/de-thi/giao-vien/{gvId}?monHocId=&trangThai=&keyword=
     */
    @GetMapping("/giao-vien/{gvId}")
    public ResponseEntity<ApiResponse<List<AdminDeThiItemDTO>>> layDeThiCuaGV(
            @PathVariable String gvId,
            @RequestParam(value = "monHocId", required = false) String monHocId,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        return ResponseEntity.ok(adminDeThiService.layDeThiTheoGiaoVien(gvId, monHocId, trangThai, keyword));
    }

    /**
     * Chi tiết 1 đề thi (bao gồm danh sách câu hỏi).
     * GET /api/admin/de-thi/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminDeThiItemDTO>> layChiTiet(
            @PathVariable String id,
            HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        ApiResponse<AdminDeThiItemDTO> res = adminDeThiService.layChiTietDeThi(id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Xóa hẳn đề thi.
     * DELETE /api/admin/de-thi/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaDeThi(
            @PathVariable String id,
            HttpServletRequest request) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<Void> res = adminDeThiService.xoaHanDeThi(id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Danh sách môn học (dropdown filter).
     * GET /api/admin/de-thi/mon-hoc
     */
    @GetMapping("/mon-hoc")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> layMonHoc(
            HttpServletRequest request) {
        if (!laAdmin(request)) return traVeLoi401();
        return ResponseEntity.ok(adminDeThiService.layDanhSachMonHoc());
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
