package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.CapNhatHoSoGiaoVienDTO;
import com.example.webthitracnghiem.dto.DoiMatKhauDTO;
import com.example.webthitracnghiem.dto.NguoiDungDTO;
import com.example.webthitracnghiem.service.AdminHoSoService;
import com.example.webthitracnghiem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API hồ sơ admin — yêu cầu JWT hợp lệ và vai trò ADMIN.
 */
@RestController
@RequestMapping("/api/admin/ho-so")
public class AdminHoSoController {

    private final AdminHoSoService adminHoSoService;

    public AdminHoSoController(AdminHoSoService adminHoSoService) {
        this.adminHoSoService = adminHoSoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NguoiDungDTO>> layHoSo(HttpServletRequest request) {
        String userId = layUserIdAdminTuJwt(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<NguoiDungDTO> res = adminHoSoService.layHoSo(userId);
        if (!res.isSuccess()) {
            HttpStatus st = res.getErrorCode() == AuthService.ERR_VAI_TRO_KHONG_HOP_LE
                    ? HttpStatus.FORBIDDEN : HttpStatus.NOT_FOUND;
            return ResponseEntity.status(st).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<NguoiDungDTO>> capNhat(
            HttpServletRequest request,
            @RequestBody CapNhatHoSoGiaoVienDTO dto) {
        String userId = layUserIdAdminTuJwt(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<NguoiDungDTO> res = adminHoSoService.capNhatHoSo(userId, dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapLoiHoSo(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/doi-mat-khau")
    public ResponseEntity<ApiResponse<Void>> doiMatKhau(
            HttpServletRequest request,
            @RequestBody DoiMatKhauDTO dto) {
        String userId = layUserIdAdminTuJwt(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền quản trị.", AuthService.ERR_HE_THONG));
        }
        ApiResponse<Void> res = adminHoSoService.doiMatKhau(userId, dto);
        if (!res.isSuccess()) {
            return ResponseEntity.status(mapLoiDoiMatKhau(res.getErrorCode())).body(res);
        }
        return ResponseEntity.ok(res);
    }

    private static String layUserIdAdminTuJwt(HttpServletRequest request) {
        Object id = request.getAttribute("jwtUserId");
        Object role = request.getAttribute("jwtVaiTro");
        if (id == null || role == null) {
            return null;
        }
        if (!AuthService.ROLE_ADMIN.equals(role.toString())) {
            return null;
        }
        return id.toString();
    }

    private static HttpStatus mapLoiHoSo(int code) {
        if (code == AuthService.ERR_VAI_TRO_KHONG_HOP_LE) {
            return HttpStatus.FORBIDDEN;
        }
        if (code == AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private static HttpStatus mapLoiDoiMatKhau(int code) {
        if (code == AuthService.ERR_VAI_TRO_KHONG_HOP_LE) {
            return HttpStatus.FORBIDDEN;
        }
        if (code == AuthService.ERR_SAI_MAT_KHAU) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code == AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
