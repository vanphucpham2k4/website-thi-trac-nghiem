package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.SinhVienThiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Làm bài / xem kết quả khi thi ẩn danh qua link — JWT loại {@link AuthService#ROLE_THI_AN_DANH}.
 */
@RestController
@RequestMapping("/api/thi-an-danh/phien")
public class ThiAnDanhApiController {

    private final SinhVienThiService sinhVienThiService;

    public ThiAnDanhApiController(SinhVienThiService sinhVienThiService) {
        this.sinhVienThiService = sinhVienThiService;
    }

    @GetMapping("/{phienThiId}/noi-dung")
    public ResponseEntity<ApiResponse<SinhVienBaiThiDTO>> noiDung(
            HttpServletRequest request,
            @PathVariable String phienThiId) {
        if (!xacMinhTokenAnDanh(request, phienThiId)) {
            return unauthorized();
        }
        ApiResponse<SinhVienBaiThiDTO> res = sinhVienThiService.layNoiDungBaiThiAnDanh(phienThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{phienThiId}/luu-tra-loi")
    public ResponseEntity<ApiResponse<Void>> luuTraLoi(
            HttpServletRequest request,
            @PathVariable String phienThiId,
            @RequestBody SinhVienLuuTraLoiDTO body) {
        if (!xacMinhTokenAnDanh(request, phienThiId)) {
            return unauthorized();
        }
        ApiResponse<Void> res = sinhVienThiService.luuTraLoiAnDanh(phienThiId, body);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{phienThiId}/nop-bai")
    public ResponseEntity<ApiResponse<SinhVienKetQuaThiDTO>> nopBai(
            HttpServletRequest request,
            @PathVariable String phienThiId) {
        if (!xacMinhTokenAnDanh(request, phienThiId)) {
            return unauthorized();
        }
        ApiResponse<SinhVienKetQuaThiDTO> res = sinhVienThiService.nopBaiAnDanh(phienThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{phienThiId}/ket-qua")
    public ResponseEntity<ApiResponse<SinhVienKetQuaThiDTO>> ketQua(
            HttpServletRequest request,
            @PathVariable String phienThiId) {
        if (!xacMinhTokenAnDanh(request, phienThiId)) {
            return unauthorized();
        }
        ApiResponse<SinhVienKetQuaThiDTO> res = sinhVienThiService.layKetQuaAnDanh(phienThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{phienThiId}/chi-tiet")
    public ResponseEntity<ApiResponse<SinhVienLichSuChiTietDTO>> chiTiet(
            HttpServletRequest request,
            @PathVariable String phienThiId) {
        if (!xacMinhTokenAnDanh(request, phienThiId)) {
            return unauthorized();
        }
        ApiResponse<SinhVienLichSuChiTietDTO> res = sinhVienThiService.layChiTietLichSuAnDanh(phienThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    private static boolean xacMinhTokenAnDanh(HttpServletRequest request, String phienThiId) {
        Object role = request.getAttribute("jwtVaiTro");
        if (!AuthService.ROLE_THI_AN_DANH.equals(String.valueOf(role))) {
            return false;
        }
        Object sub = request.getAttribute("jwtUserId");
        return sub != null && phienThiId.equals(sub.toString());
    }

    private static <T> ResponseEntity<ApiResponse<T>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Phiên làm bài không hợp lệ hoặc đã hết hạn.", AuthService.ERR_HE_THONG));
    }
}
