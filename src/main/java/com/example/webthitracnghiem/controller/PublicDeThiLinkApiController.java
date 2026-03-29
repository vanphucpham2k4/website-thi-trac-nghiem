package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.BatDauThiAnDanhRequestDTO;
import com.example.webthitracnghiem.dto.BatDauThiAnDanhResponseDTO;
import com.example.webthitracnghiem.dto.PublicDeThiLinkThongTinDTO;
import com.example.webthitracnghiem.service.DeThiLinkCongKhaiService;
import com.example.webthitracnghiem.service.SinhVienThiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API công khai — xem thông tin đề qua mã link (không cần JWT).
 */
@RestController
@RequestMapping("/api/public/de-thi-link")
public class PublicDeThiLinkApiController {

    private final DeThiLinkCongKhaiService deThiLinkCongKhaiService;
    private final SinhVienThiService sinhVienThiService;

    public PublicDeThiLinkApiController(
            DeThiLinkCongKhaiService deThiLinkCongKhaiService,
            SinhVienThiService sinhVienThiService) {
        this.deThiLinkCongKhaiService = deThiLinkCongKhaiService;
        this.sinhVienThiService = sinhVienThiService;
    }

    @GetMapping("/{maTruyCap}/thong-tin")
    public ResponseEntity<ApiResponse<PublicDeThiLinkThongTinDTO>> thongTin(@PathVariable("maTruyCap") String maTruyCap) {
        ApiResponse<PublicDeThiLinkThongTinDTO> res = deThiLinkCongKhaiService.layThongTinCongKhaiTheoMa(maTruyCap);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Bắt đầu / tiếp tục phiên thi ẩn danh (chỉ cần họ tên).
     */
    @PostMapping("/{maTruyCap}/bat-dau-an-danh")
    public ResponseEntity<ApiResponse<BatDauThiAnDanhResponseDTO>> batDauAnDanh(
            @PathVariable("maTruyCap") String maTruyCap,
            @RequestBody(required = false) BatDauThiAnDanhRequestDTO body) {
        String hoTen = body != null ? body.getHoTen() : null;
        ApiResponse<BatDauThiAnDanhResponseDTO> res = sinhVienThiService.batDauThiAnDanhQuaLink(maTruyCap, hoTen);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        return ResponseEntity.ok(res);
    }
}
