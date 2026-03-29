package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.GiaoVienLopHocChiTietDTO;
import com.example.webthitracnghiem.dto.GiaoVienLopHocListItemDTO;
import com.example.webthitracnghiem.dto.LopHocTaoCapNhatDTO;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.LopHocService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Quản lý lớp học (giáo viên chủ trì).
 */
@RestController
@RequestMapping("/api/giao-vien/lop-hoc")
public class GiaoVienLopHocController {

    private final LopHocService lopHocService;

    public GiaoVienLopHocController(LopHocService lopHocService) {
        this.lopHocService = lopHocService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GiaoVienLopHocListItemDTO>>> danhSach(HttpServletRequest request) {
        String gvId = layUserIdGiaoVienTuJwt(request);
        if (gvId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<List<GiaoVienLopHocListItemDTO>> res = lopHocService.layDanhSachGiaoVien(gvId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{lopId}")
    public ResponseEntity<ApiResponse<GiaoVienLopHocChiTietDTO>> chiTiet(
            HttpServletRequest request,
            @PathVariable String lopId) {
        String gvId = layUserIdGiaoVienTuJwt(request);
        if (gvId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<GiaoVienLopHocChiTietDTO> res = lopHocService.layChiTietGiaoVien(gvId, lopId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GiaoVienLopHocListItemDTO>> tao(
            HttpServletRequest request,
            @RequestBody LopHocTaoCapNhatDTO dto) {
        String gvId = layUserIdGiaoVienTuJwt(request);
        if (gvId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<GiaoVienLopHocListItemDTO> res = lopHocService.taoLop(gvId, dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{lopId}")
    public ResponseEntity<ApiResponse<GiaoVienLopHocListItemDTO>> capNhat(
            HttpServletRequest request,
            @PathVariable String lopId,
            @RequestBody LopHocTaoCapNhatDTO dto) {
        String gvId = layUserIdGiaoVienTuJwt(request);
        if (gvId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<GiaoVienLopHocListItemDTO> res = lopHocService.capNhatLop(gvId, lopId, dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{lopId}")
    public ResponseEntity<ApiResponse<Void>> xoa(HttpServletRequest request, @PathVariable String lopId) {
        String gvId = layUserIdGiaoVienTuJwt(request);
        if (gvId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<Void> res = lopHocService.xoaLop(gvId, lopId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    private static String layUserIdGiaoVienTuJwt(HttpServletRequest request) {
        Object id = request.getAttribute("jwtUserId");
        Object role = request.getAttribute("jwtVaiTro");
        if (id == null || role == null) {
            return null;
        }
        if (!AuthService.ROLE_GIAO_VIEN.equals(role.toString())) {
            return null;
        }
        return id.toString();
    }
}
