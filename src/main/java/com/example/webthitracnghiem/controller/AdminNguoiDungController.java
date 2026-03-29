package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.service.AdminNguoiDungService;
import com.example.webthitracnghiem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API quản lý người dùng — chỉ Admin (JWT vai trò ADMIN).
 */
@RestController
@RequestMapping("/api/admin/nguoi-dung")
public class AdminNguoiDungController {

    private final AdminNguoiDungService adminNguoiDungService;

    public AdminNguoiDungController(AdminNguoiDungService adminNguoiDungService) {
        this.adminNguoiDungService = adminNguoiDungService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminNguoiDungItemDTO>>> danhSach(HttpServletRequest request) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        return ResponseEntity.ok(adminNguoiDungService.danhSachTatCa());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminNguoiDungItemDTO>> chiTiet(
            HttpServletRequest request,
            @PathVariable String id) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<AdminNguoiDungItemDTO> res = adminNguoiDungService.chiTiet(id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminNguoiDungItemDTO>> capNhat(
            HttpServletRequest request,
            @PathVariable String id,
            @RequestBody AdminCapNhatNguoiDungDTO dto) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<AdminNguoiDungItemDTO> res = adminNguoiDungService.capNhat(id, dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/mat-khau")
    public ResponseEntity<ApiResponse<Void>> datLaiMatKhau(
            HttpServletRequest request,
            @PathVariable String id,
            @RequestBody AdminDatLaiMatKhauDTO dto) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<Void> res = adminNguoiDungService.datLaiMatKhau(id, dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/vai-tro")
    public ResponseEntity<ApiResponse<AdminNguoiDungItemDTO>> doiVaiTro(
            HttpServletRequest request,
            @PathVariable String id,
            @RequestBody AdminDoiVaiTroDTO dto) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<AdminNguoiDungItemDTO> res = adminNguoiDungService.doiVaiTro(id, dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoa(
            HttpServletRequest request,
            @PathVariable String id) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        String adminId = request.getAttribute("jwtUserId").toString();
        ApiResponse<Void> res = adminNguoiDungService.xoaNguoiDung(adminId, id);
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
