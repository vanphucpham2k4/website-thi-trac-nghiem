package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.SinhVienDeThiTrongLopDTO;
import com.example.webthitracnghiem.dto.SinhVienLopPhongThiChiTietDTO;
import com.example.webthitracnghiem.dto.SinhVienLopPhongThiItemDTO;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.LopHocService;
import com.example.webthitracnghiem.service.SinhVienThiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API Lớp/Phòng thi (sinh viên): các lớp đã được thêm vào.
 */
@RestController
@RequestMapping("/api/sinh-vien/lop-phong-thi")
public class SinhVienLopPhongThiController {

    private final LopHocService lopHocService;
    private final SinhVienThiService sinhVienThiService;

    public SinhVienLopPhongThiController(LopHocService lopHocService, SinhVienThiService sinhVienThiService) {
        this.lopHocService = lopHocService;
        this.sinhVienThiService = sinhVienThiService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SinhVienLopPhongThiItemDTO>>> danhSach(HttpServletRequest request) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<List<SinhVienLopPhongThiItemDTO>> res = lopHocService.layDanhSachChoSinhVien(svId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{lopId}")
    public ResponseEntity<ApiResponse<SinhVienLopPhongThiChiTietDTO>> chiTiet(
            HttpServletRequest request,
            @PathVariable String lopId) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<SinhVienLopPhongThiChiTietDTO> res = lopHocService.layChiTietChoSinhVien(svId, lopId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Đề thi đã xuất bản trong lớp (danh sách cho trang chi tiết lớp).
     */
    @GetMapping("/{lopId}/de-thi")
    public ResponseEntity<ApiResponse<List<SinhVienDeThiTrongLopDTO>>> deThiTrongLop(
            HttpServletRequest request,
            @PathVariable String lopId) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<List<SinhVienDeThiTrongLopDTO>> res = sinhVienThiService.layDeThiTrongLop(svId, lopId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    private static String layUserIdSinhVienTuJwt(HttpServletRequest request) {
        Object id = request.getAttribute("jwtUserId");
        Object role = request.getAttribute("jwtVaiTro");
        if (id == null || role == null) {
            return null;
        }
        if (!AuthService.ROLE_SINH_VIEN.equals(role.toString())) {
            return null;
        }
        return id.toString();
    }
}
