package com.example.webthitracnghiem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController - Xử lý các yêu cầu liên quan đến trang chủ và một số trang gốc (không prefix)
 */
@Controller
public class HomeController {

    /**
     * Trang chủ — URL: GET /
     */
    @GetMapping("/")
    public String index() {
        return "home";
    }

    /**
     * Dashboard Admin — URL: GET /admin
     * (Không đặt trong DashboardController vì class đó có @RequestMapping("/dashboard")
     * nên /admin ở đó sẽ thành /dashboard/admin và gây 404 khi frontend redirect /admin)
     */
    @GetMapping("/admin")
    public String trangDashboardAdmin() {
        return "dashboard-admin";
    }
}
