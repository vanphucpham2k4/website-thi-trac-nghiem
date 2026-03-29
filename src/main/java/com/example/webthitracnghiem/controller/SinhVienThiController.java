package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.SinhVienBaiThiDTO;
import com.example.webthitracnghiem.dto.SinhVienBatDauThiResponseDTO;
import com.example.webthitracnghiem.dto.SinhVienKetQuaThiDTO;
import com.example.webthitracnghiem.dto.SinhVienLuuTraLoiDTO;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.SinhVienThiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API làm bài thi (sinh viên): bắt đầu phiên, tải đề, lưu đáp án, nộp bài, xem kết quả.
 */
@RestController
@RequestMapping("/api/sinh-vien/thi")
public class SinhVienThiController {

    private final SinhVienThiService sinhVienThiService;

    public SinhVienThiController(SinhVienThiService sinhVienThiService) {
        this.sinhVienThiService = sinhVienThiService;
    }

    @PostMapping("/lop/{lopId}/de-thi/{deThiId}/bat-dau")
    public ResponseEntity<ApiResponse<SinhVienBatDauThiResponseDTO>> batDau(
            HttpServletRequest request,
            @PathVariable String lopId,
            @PathVariable String deThiId) {
        String svId = layUserIdSinhVien(request);
        if (svId == null) {
            return unauthorized();
        }
        ApiResponse<SinhVienBatDauThiResponseDTO> res = sinhVienThiService.batDauThi(svId, lopId, deThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Bắt đầu / tiếp tục làm bài qua link công khai (mã trên URL /thi-mo/{ma}).
     */
    @PostMapping("/link/{maTruyCap}/bat-dau")
    public ResponseEntity<ApiResponse<SinhVienBatDauThiResponseDTO>> batDauQuaLink(
            HttpServletRequest request,
            @PathVariable("maTruyCap") String maTruyCap) {
        String svId = layUserIdSinhVien(request);
        if (svId == null) {
            return unauthorized();
        }
        ApiResponse<SinhVienBatDauThiResponseDTO> res = sinhVienThiService.batDauThiQuaLinkCongKhai(svId, maTruyCap);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/phien/{phienThiId}/noi-dung")
    public ResponseEntity<ApiResponse<SinhVienBaiThiDTO>> noiDung(
            HttpServletRequest request,
            @PathVariable String phienThiId) {
        String svId = layUserIdSinhVien(request);
        if (svId == null) {
            return unauthorized();
        }
        ApiResponse<SinhVienBaiThiDTO> res = sinhVienThiService.layNoiDungBaiThi(svId, phienThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping("/phien/{phienThiId}/luu-tra-loi")
    public ResponseEntity<ApiResponse<Void>> luuTraLoi(
            HttpServletRequest request,
            @PathVariable String phienThiId,
            @RequestBody SinhVienLuuTraLoiDTO body) {
        String svId = layUserIdSinhVien(request);
        if (svId == null) {
            return unauthorized();
        }
        ApiResponse<Void> res = sinhVienThiService.luuTraLoi(svId, phienThiId, body);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/phien/{phienThiId}/nop-bai")
    public ResponseEntity<ApiResponse<SinhVienKetQuaThiDTO>> nopBai(
            HttpServletRequest request,
            @PathVariable String phienThiId) {
        String svId = layUserIdSinhVien(request);
        if (svId == null) {
            return unauthorized();
        }
        ApiResponse<SinhVienKetQuaThiDTO> res = sinhVienThiService.nopBai(svId, phienThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/phien/{phienThiId}/ket-qua")
    public ResponseEntity<ApiResponse<SinhVienKetQuaThiDTO>> ketQua(
            HttpServletRequest request,
            @PathVariable String phienThiId) {
        String svId = layUserIdSinhVien(request);
        if (svId == null) {
            return unauthorized();
        }
        ApiResponse<SinhVienKetQuaThiDTO> res = sinhVienThiService.layKetQua(svId, phienThiId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    private static String layUserIdSinhVien(HttpServletRequest request) {
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

    private static <T> ResponseEntity<ApiResponse<T>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
    }
}
