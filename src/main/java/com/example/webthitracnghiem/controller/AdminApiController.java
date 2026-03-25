package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.AdminDashboardDTO;
import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API dành cho Admin — mapping gốc /api/admin/...
 * (Tách khỏi DashboardController vì class đó có prefix /dashboard,
 * khiến /api/admin/dashboard thực tế thành /dashboard/api/admin/dashboard)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final DashboardService dashboardService;

    public AdminApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * API lấy dữ liệu dashboard admin
     * URL: GET /api/admin/dashboard?userId=xxx
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> layDashboardAdmin(
            @RequestParam("userId") String userId
    ) {
        AdminDashboardDTO dto = dashboardService.layDashboardAdmin(userId);

        if (dto == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Không tìm thấy admin!", 1));
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", dto));
    }
}
