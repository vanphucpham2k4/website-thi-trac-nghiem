package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.AdminCapNhatYeuCauDoiThuongDTO;
import com.example.webthitracnghiem.dto.AdminYeuCauDoiThuongChiTietDTO;
import com.example.webthitracnghiem.dto.AdminYeuCauDoiThuongItemDTO;
import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.service.AdminDoiThuongService;
import com.example.webthitracnghiem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quản lý yêu cầu đổi thưởng — chỉ ADMIN.
 */
@RestController
@RequestMapping("/api/admin/doi-thuong/yeu-cau")
public class AdminDoiThuongController {

    private final AdminDoiThuongService adminDoiThuongService;

    public AdminDoiThuongController(AdminDoiThuongService adminDoiThuongService) {
        this.adminDoiThuongService = adminDoiThuongService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminYeuCauDoiThuongItemDTO>>> danhSach(
            HttpServletRequest request,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "q", required = false) String q) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        return ResponseEntity.ok(adminDoiThuongService.danhSach(trangThai, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminYeuCauDoiThuongChiTietDTO>> chiTiet(
            HttpServletRequest request,
            @PathVariable String id) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<AdminYeuCauDoiThuongChiTietDTO> res = adminDoiThuongService.chiTiet(id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminYeuCauDoiThuongChiTietDTO>> capNhat(
            HttpServletRequest request,
            @PathVariable String id,
            @RequestBody AdminCapNhatYeuCauDoiThuongDTO dto) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<AdminYeuCauDoiThuongChiTietDTO> res = adminDoiThuongService.capNhat(id, dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoa(HttpServletRequest request, @PathVariable String id) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<Void> res = adminDoiThuongService.xoa(id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    private static boolean laAdmin(HttpServletRequest request) {
        Object role = request.getAttribute("jwtVaiTro");
        Object uid = request.getAttribute("jwtUserId");
        return uid != null && AuthService.ROLE_ADMIN.equals(String.valueOf(role));
    }
}
