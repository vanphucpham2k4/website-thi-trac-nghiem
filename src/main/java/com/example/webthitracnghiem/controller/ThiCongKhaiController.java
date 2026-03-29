package com.example.webthitracnghiem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Trang công khai: link đề thi ẩn danh (họ tên, không đăng nhập).
 */
@Controller
public class ThiCongKhaiController {

    @GetMapping("/thi-mo/{maTruyCap}")
    public String trangThiMoCongKhai(@PathVariable String maTruyCap, Model model) {
        model.addAttribute("maTruyCap", maTruyCap);
        return "thi-mo-cong-khai";
    }

    @GetMapping("/thi-mo/lam-bai/{phienThiId}")
    public String lamBaiAnDanh(@PathVariable String phienThiId, Model model) {
        model.addAttribute("phienThiId", phienThiId);
        model.addAttribute("thiAnDanh", true);
        return "sinh-vien-lam-bai";
    }

    @GetMapping("/thi-mo/ket-qua/{phienThiId}")
    public String ketQuaAnDanh(@PathVariable String phienThiId, Model model) {
        model.addAttribute("phienThiId", phienThiId);
        model.addAttribute("thiAnDanh", true);
        return "sinh-vien-ket-qua-thi";
    }

    @GetMapping("/thi-mo/chi-tiet/{phienThiId}")
    public String chiTietAnDanh(@PathVariable String phienThiId, Model model) {
        model.addAttribute("phienThiId", phienThiId);
        model.addAttribute("thiAnDanh", true);
        return "sinh-vien-lich-su-chi-tiet";
    }
}
