package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.TheoDoiDeThiOptionDTO;
import com.example.webthitracnghiem.dto.TheoDoiSinhVienThiDTO;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.TheoDoiThiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/giao-vien/theo-doi-thi")
public class TheoDoiThiController {

    private final TheoDoiThiService theoDoiThiService;

    public TheoDoiThiController(TheoDoiThiService theoDoiThiService) {
        this.theoDoiThiService = theoDoiThiService;
    }

    @GetMapping("/de-thi")
    public ResponseEntity<ApiResponse<List<TheoDoiDeThiOptionDTO>>> layDanhSachDeThi(HttpServletRequest request) {
        String giaoVienId = layUserIdGiaoVienTuJwt(request);
        if (giaoVienId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
        }

        List<TheoDoiDeThiOptionDTO> danhSach = theoDoiThiService.layDanhSachDeThiTheoGiaoVien(giaoVienId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đề thi thành công.", danhSach));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<TheoDoiSinhVienThiDTO>> layDanhSachTheoDoi(
            HttpServletRequest request,
            @RequestParam("deThiId") String deThiId,
            @RequestParam(value = "nhomTrangThai", required = false) String nhomTrangThai,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        String giaoVienId = layUserIdGiaoVienTuJwt(request);
        if (giaoVienId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Phiên đăng nhập không hợp lệ hoặc không có quyền giáo viên.", AuthService.ERR_HE_THONG));
        }

        TheoDoiSinhVienThiDTO dto = theoDoiThiService.layDanhSachTheoDoi(giaoVienId, deThiId, nhomTrangThai, keyword);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy đề thi hoặc bạn không có quyền xem.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI));
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách theo dõi thành công.", dto));
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
