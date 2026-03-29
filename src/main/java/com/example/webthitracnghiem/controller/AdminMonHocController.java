package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.AdminLuuMonHocDTO;
import com.example.webthitracnghiem.dto.AdminMonHocItemDTO;
import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.service.AdminMonHocService;
import com.example.webthitracnghiem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API quản lý môn học — chỉ Admin (JWT vai trò ADMIN).
 */
@RestController
@RequestMapping("/api/admin/mon-hoc")
public class AdminMonHocController {

    private final AdminMonHocService adminMonHocService;

    public AdminMonHocController(AdminMonHocService adminMonHocService) {
        this.adminMonHocService = adminMonHocService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminMonHocItemDTO>>> danhSach(HttpServletRequest request) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        return ResponseEntity.ok(adminMonHocService.danhSach());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminMonHocItemDTO>> chiTiet(HttpServletRequest request, @PathVariable String id) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<AdminMonHocItemDTO> res = adminMonHocService.chiTiet(id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminMonHocItemDTO>> taoMoi(
            HttpServletRequest request,
            @RequestBody AdminLuuMonHocDTO dto) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<AdminMonHocItemDTO> res = adminMonHocService.taoMoi(dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminMonHocItemDTO>> capNhat(
            HttpServletRequest request,
            @PathVariable String id,
            @RequestBody AdminLuuMonHocDTO dto) {
        if (!laAdmin(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<AdminMonHocItemDTO> res = adminMonHocService.capNhat(id, dto);
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
        ApiResponse<Void> res = adminMonHocService.xoa(id);
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
