package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.SinhVienLichSuChiTietDTO;
import com.example.webthitracnghiem.dto.SinhVienLichSuThiItemDTO;
import com.example.webthitracnghiem.service.AuthService;
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
 * API lịch sử thi & xem lại chi tiết (sinh viên).
 */
@RestController
@RequestMapping("/api/sinh-vien/lich-su-thi")
public class SinhVienLichSuThiApiController {

    private final SinhVienThiService sinhVienThiService;

    public SinhVienLichSuThiApiController(SinhVienThiService sinhVienThiService) {
        this.sinhVienThiService = sinhVienThiService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SinhVienLichSuThiItemDTO>>> danhSach(HttpServletRequest request) {
        String svId = layUserId(request);
        if (svId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<List<SinhVienLichSuThiItemDTO>> res = sinhVienThiService.layLichSu(svId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{phienThiId}/chi-tiet")
    public ResponseEntity<ApiResponse<SinhVienLichSuChiTietDTO>> chiTiet(
            HttpServletRequest request,
            @PathVariable String phienThiId) {
        String svId = layUserId(request);
        if (svId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<SinhVienLichSuChiTietDTO> res = sinhVienThiService.layChiTietLichSu(svId, phienThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    private static String layUserId(HttpServletRequest request) {
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
