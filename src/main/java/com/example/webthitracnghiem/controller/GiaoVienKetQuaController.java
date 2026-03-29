package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.GiaoVienKetQuaDeThiItemDTO;
import com.example.webthitracnghiem.dto.GiaoVienKetQuaLopItemDTO;
import com.example.webthitracnghiem.dto.GiaoVienKetQuaSinhVienItemDTO;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.GiaoVienKetQuaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API Xem kết quả chi tiết (giáo viên).
 * Luồng 1: Lớp → Đề thi → Kết quả (lớp + ẩn danh).
 * Luồng 2: Tất cả đề thi → Kết quả toàn bộ.
 */
@RestController
@RequestMapping("/api/giao-vien/ket-qua")
public class GiaoVienKetQuaController {

    private final GiaoVienKetQuaService ketQuaService;

    public GiaoVienKetQuaController(GiaoVienKetQuaService ketQuaService) {
        this.ketQuaService = ketQuaService;
    }

    // ===== Luồng 1: Theo Lớp =====

    /** Bước 1: Danh sách lớp học */
    @GetMapping("/lop")
    public ResponseEntity<ApiResponse<List<GiaoVienKetQuaLopItemDTO>>> danhSachLop(HttpServletRequest request) {
        String gvId = layGiaoVienId(request);
        if (gvId == null) return unauthorized();
        ApiResponse<List<GiaoVienKetQuaLopItemDTO>> res = ketQuaService.layDanhSachLop(gvId);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    /** Bước 2: Danh sách đề thi đã xuất bản cho lớp */
    @GetMapping("/lop/{lopId}/de-thi")
    public ResponseEntity<ApiResponse<List<GiaoVienKetQuaDeThiItemDTO>>> danhSachDeThi(
            HttpServletRequest request, @PathVariable String lopId) {
        String gvId = layGiaoVienId(request);
        if (gvId == null) return unauthorized();
        ApiResponse<List<GiaoVienKetQuaDeThiItemDTO>> res = ketQuaService.layDanhSachDeThiCuaLop(gvId, lopId);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    /** Bước 3: Kết quả sinh viên (lớp + ẩn danh) */
    @GetMapping("/lop/{lopId}/de-thi/{deThiId}")
    public ResponseEntity<ApiResponse<List<GiaoVienKetQuaSinhVienItemDTO>>> ketQuaSinhVien(
            HttpServletRequest request,
            @PathVariable String lopId,
            @PathVariable String deThiId) {
        String gvId = layGiaoVienId(request);
        if (gvId == null) return unauthorized();
        ApiResponse<List<GiaoVienKetQuaSinhVienItemDTO>> res = ketQuaService.layKetQuaSinhVien(gvId, lopId, deThiId);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    // ===== Luồng 2: Tất cả đề thi =====

    /** Danh sách tất cả đề thi của giáo viên */
    @GetMapping("/de-thi")
    public ResponseEntity<ApiResponse<List<GiaoVienKetQuaDeThiItemDTO>>> danhSachTatCaDeThi(
            HttpServletRequest request) {
        String gvId = layGiaoVienId(request);
        if (gvId == null) return unauthorized();
        ApiResponse<List<GiaoVienKetQuaDeThiItemDTO>> res = ketQuaService.layDanhSachDeThiCuaGiaoVien(gvId);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    /** Kết quả toàn bộ của một đề thi (mọi lớp + ẩn danh) */
    @GetMapping("/de-thi/{deThiId}")
    public ResponseEntity<ApiResponse<List<GiaoVienKetQuaSinhVienItemDTO>>> ketQuaTheoDeThiId(
            HttpServletRequest request,
            @PathVariable String deThiId) {
        String gvId = layGiaoVienId(request);
        if (gvId == null) return unauthorized();
        ApiResponse<List<GiaoVienKetQuaSinhVienItemDTO>> res = ketQuaService.layKetQuaTheoDeThiId(gvId, deThiId);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    // ===== Ghi chú =====

    /** Cập nhật ghi chú cho một kết quả thi */
    @PutMapping("/ghi-chu/{ketQuaThiId}")
    public ResponseEntity<ApiResponse<Void>> capNhatGhiChu(
            HttpServletRequest request,
            @PathVariable String ketQuaThiId,
            @RequestBody Map<String, String> body) {
        String gvId = layGiaoVienId(request);
        if (gvId == null) return unauthorized();
        String ghiChu = body.getOrDefault("ghiChu", "");
        ApiResponse<Void> res = ketQuaService.capNhatGhiChu(gvId, ketQuaThiId, ghiChu);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    /** Cập nhật điểm cho một kết quả thi */
    @PutMapping("/diem/{ketQuaThiId}")
    public ResponseEntity<ApiResponse<Void>> capNhatDiem(
            HttpServletRequest request,
            @PathVariable String ketQuaThiId,
            @RequestBody Map<String, String> body) {
        String gvId = layGiaoVienId(request);
        if (gvId == null) return unauthorized();
        String diemStr = body.getOrDefault("diem", "");
        java.math.BigDecimal diem = null;
        if (diemStr != null && !diemStr.isBlank()) {
            try {
                diem = new java.math.BigDecimal(diemStr.trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Điểm không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE));
            }
        }
        ApiResponse<Void> res = ketQuaService.capNhatDiem(gvId, ketQuaThiId, diem);
        return res.isSuccess() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    // ===== Helper =====

    private static String layGiaoVienId(HttpServletRequest request) {
        Object id = request.getAttribute("jwtUserId");
        Object role = request.getAttribute("jwtVaiTro");
        if (id == null || role == null) return null;
        if (!AuthService.ROLE_GIAO_VIEN.equals(role.toString())) return null;
        return id.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T> ResponseEntity<ApiResponse<T>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
    }
}
