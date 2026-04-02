package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.DoiThuongRequestDTO;
import com.example.webthitracnghiem.dto.DoiThuongTongQuanDTO;
import com.example.webthitracnghiem.dto.PhanThuongCardDTO;
import com.example.webthitracnghiem.dto.YeuCauDoiThuongDTO;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.DoiThuongService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API Đổi thưởng (sinh viên).
 */
@RestController
@RequestMapping("/api/sinh-vien/doi-thuong")
public class SinhVienDoiThuongController {

    private final DoiThuongService doiThuongService;

    public SinhVienDoiThuongController(DoiThuongService doiThuongService) {
        this.doiThuongService = doiThuongService;
    }

    @GetMapping("/tong-quan")
    public ResponseEntity<ApiResponse<DoiThuongTongQuanDTO>> tongQuan(HttpServletRequest request) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return loi401();
        }
        ApiResponse<DoiThuongTongQuanDTO> res = doiThuongService.layTongQuan(svId);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/phan-thuong")
    public ResponseEntity<ApiResponse<List<PhanThuongCardDTO>>> phanThuong(
            HttpServletRequest request,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "loai", required = false) String loai,
            @RequestParam(value = "mucDiem", required = false) String mucDiem,
            @RequestParam(value = "locTrangThai", required = false) String locTrangThai) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return loi401List();
        }
        ApiResponse<List<PhanThuongCardDTO>> res =
                doiThuongService.layPhanThuongCards(svId, q, loai, mucDiem, locTrangThai);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/lich-su")
    public ResponseEntity<ApiResponse<List<YeuCauDoiThuongDTO>>> lichSu(
            HttpServletRequest request,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "khoang", required = false) String khoang) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return loi401ListYeuCau();
        }
        ApiResponse<List<YeuCauDoiThuongDTO>> res = doiThuongService.layLichSu(svId, q, trangThai, khoang);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/lich-su/{id}")
    public ResponseEntity<ApiResponse<YeuCauDoiThuongDTO>> chiTiet(
            HttpServletRequest request,
            @PathVariable("id") String id) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return loi401YeuCau();
        }
        ApiResponse<YeuCauDoiThuongDTO> res = doiThuongService.layChiTiet(svId, id);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/doi")
    public ResponseEntity<ApiResponse<YeuCauDoiThuongDTO>> doi(
            HttpServletRequest request,
            @RequestBody DoiThuongRequestDTO body) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return loi401YeuCau();
        }
        ApiResponse<YeuCauDoiThuongDTO> res = doiThuongService.doiThuong(svId, body);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/huy/{id}")
    public ResponseEntity<ApiResponse<YeuCauDoiThuongDTO>> huy(
            HttpServletRequest request,
            @PathVariable("id") String id) {
        String svId = layUserIdSinhVienTuJwt(request);
        if (svId == null) {
            return loi401YeuCau();
        }
        ApiResponse<YeuCauDoiThuongDTO> res = doiThuongService.huyYeuCau(svId, id);
        if (!res.isSuccess()) {
            int code = res.getErrorCode();
            HttpStatus st = code == AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(st).body(res);
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

    private static ResponseEntity<ApiResponse<DoiThuongTongQuanDTO>> loi401() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<DoiThuongTongQuanDTO>error(
                        "Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
    }

    private static ResponseEntity<ApiResponse<List<PhanThuongCardDTO>>> loi401List() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<List<PhanThuongCardDTO>>error(
                        "Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
    }

    private static ResponseEntity<ApiResponse<List<YeuCauDoiThuongDTO>>> loi401ListYeuCau() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<List<YeuCauDoiThuongDTO>>error(
                        "Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
    }

    private static ResponseEntity<ApiResponse<YeuCauDoiThuongDTO>> loi401YeuCau() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<YeuCauDoiThuongDTO>error(
                        "Phiên đăng nhập không hợp lệ hoặc không có quyền sinh viên.", AuthService.ERR_HE_THONG));
    }
}
