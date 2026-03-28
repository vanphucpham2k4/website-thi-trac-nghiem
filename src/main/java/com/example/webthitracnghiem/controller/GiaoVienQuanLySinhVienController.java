package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.GiaoVienSinhVienListItemDTO;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.GiaoVienQuanLySinhVienService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API Quản lý sinh viên (giáo viên): xem danh sách sinh viên + lượt thi trên đề của mình.
 */
@RestController
@RequestMapping("/api/giao-vien/sinh-vien")
public class GiaoVienQuanLySinhVienController {

    private final GiaoVienQuanLySinhVienService quanLySinhVienService;

    public GiaoVienQuanLySinhVienController(GiaoVienQuanLySinhVienService quanLySinhVienService) {
        this.quanLySinhVienService = quanLySinhVienService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GiaoVienSinhVienListItemDTO>>> layDanhSach(
            HttpServletRequest request,
            @RequestParam(value = "keyword", required = false) String keyword) {

        String giaoVienId = layUserIdGiaoVienTuJwt(request);
        if (giaoVienId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
        }

        ApiResponse<List<GiaoVienSinhVienListItemDTO>> res = quanLySinhVienService.layDanhSach(giaoVienId, keyword);
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
