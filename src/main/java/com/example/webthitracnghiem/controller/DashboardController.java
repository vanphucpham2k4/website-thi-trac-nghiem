package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.GiaoVienDashboardDTO;
import com.example.webthitracnghiem.dto.SinhVienDashboardDTO;
import com.example.webthitracnghiem.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller - Xử lý Dashboard cho SINH VIÊN và GIÁO VIÊN
 * Cung cấp các API endpoints để lấy dữ liệu thống kê, bảng điểm...
 *
 * Mỗi endpoint đều nhận userId từ query param hoặc từ session/header
 * (Trong thực tế nên dùng Spring Security Session/JWT để lấy userId)
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    // ===== BEAN DEPENDENCY INJECTION =====
    private final DashboardService dashboardService;

    /**
     * Constructor injection
     * @param dashboardService Service xử lý dữ liệu dashboard
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ========================================
    // 1. TRANG VIEW (HTML)
    // ========================================

    /**
     * Trả về trang Dashboard của SINH VIÊN
     * URL: GET /dashboard/sinh-vien
     *
     * @return Template "dashboard-sinh-vien"
     */
    @GetMapping("/sinh-vien")
    public String trangDashboardSinhVien() {
        return "dashboard-sinh-vien";
    }

    /**
     * Trang quản lý hồ sơ cá nhân sinh viên
     * URL: GET /dashboard/sinh-vien/ho-so
     */
    @GetMapping("/sinh-vien/ho-so")
    public String trangHoSoSinhVien() {
        return "sinh-vien-ho-so";
    }

    /**
     * Trang Phòng thi (sinh viên) — placeholder.
     * URL: GET /dashboard/sinh-vien/phong-thi
     */
    @GetMapping("/sinh-vien/phong-thi")
    public String trangPhongThiSinhVien() {
        return "sinh-vien-phong-thi";
    }

    /**
     * Trả về trang Dashboard của GIÁO VIÊN
     * URL: GET /dashboard/giao-vien
     *
     * @return Template "dashboard-giao-vien"
     */
    @GetMapping("/giao-vien")
    public String trangDashboardGiaoVien() {
        return "dashboard-giao-vien";
    }

    @GetMapping("/giao-vien/theo-doi-thi")
    public String trangTheoDoiThiGiaoVien() {
        return "theo-doi-thi-giao-vien";
    }

    /**
     * Trang quản lý hồ sơ cá nhân giáo viên
     * URL: GET /dashboard/giao-vien/ho-so
     */
    @GetMapping("/giao-vien/ho-so")
    public String trangHoSoGiaoVien() {
        return "giao-vien-ho-so";
    }

    /**
     * Trang quản lý đề thi của giáo viên
     * URL: GET /dashboard/giao-vien/de-thi
     */
    @GetMapping("/giao-vien/de-thi")
    public String trangQuanLyDeThi() {
        return "de-thi-quan-ly";
    }

    /**
     * Trang chỉnh sửa câu hỏi trong đề (văn bản thô + xem trước).
     * URL: GET /dashboard/giao-vien/de-thi/{deThiId}/chinh-sua-cau-hoi
     */
    @GetMapping("/giao-vien/de-thi/{deThiId}/chinh-sua-cau-hoi")
    public String trangChinhSuaCauHoiTrongDe(@PathVariable String deThiId, Model model) {
        model.addAttribute("deThiId", deThiId);
        return "de-thi-chinh-sua-cau-hoi";
    }

    /**
     * Trang ngân hàng câu hỏi của giáo viên
     * URL: GET /dashboard/giao-vien/ngan-hang-cau-hoi
     */
    @GetMapping("/giao-vien/ngan-hang-cau-hoi")
    public String trangNganHangCauHoi() {
        return "ngan-hang-cau-hoi";
    }

    // ========================================
    // 2. API DASHBOARD SINH VIÊN
    // ========================================

    /**
     * API Lấy dữ liệu dashboard SINH VIÊN
     * URL: GET /api/dashboard/sinh-vien?userId=xxx
     *
     * @param userId ID của sinh viên (trong thực tế lấy từ session)
     *
     * @return ApiResponse chứa SinhVienDashboardDTO với các thông tin:
     *         - Thông tin cá nhân
     *         - Thống kê tổng quan (số lần thi, điểm trung bình...)
     *         - Điểm theo từng môn học
     *         - Bài thi gần nhất
     */
    @GetMapping("/api/sinh-vien")
    @ResponseBody
    public ResponseEntity<ApiResponse<SinhVienDashboardDTO>> layDashboardSinhVien(
            @RequestParam("userId") String userId
    ) {
        SinhVienDashboardDTO dto = dashboardService.layDashboardSinhVien(userId);

        if (dto == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Không tìm thấy sinh viên!", 1));
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", dto));
    }

    /**
     * API Lấy LỊCH SỬ THI của sinh viên
     * URL: GET /api/dashboard/sinh-vien/lich-su?userId=xxx
     *
     * @param userId ID của sinh viên
     *
     * @return ApiResponse chứa danh sách kết quả thi
     */
    @GetMapping("/api/sinh-vien/lich-su")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> layLichSuThi(
            @RequestParam("userId") String userId
    ) {
        // Trong thực tế sẽ gọi repository để lấy lịch sử thi
        // Hiện tại trả về mock data để demo
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử thi thành công", null));
    }

    /**
     * API Lấy BẢNG XẾP HẠNG
     * URL: GET /api/dashboard/xep-hang
     *
     * @return ApiResponse chứa danh sách sinh viên xếp hạng theo điểm
     */
    @GetMapping("/api/xep-hang")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> layBangXepHang() {
        // Trong thực tế sẽ gọi service để tính xếp hạng
        return ResponseEntity.ok(ApiResponse.success("Lấy bảng xếp hạng thành công", null));
    }

    // ========================================
    // 3. API DASHBOARD GIÁO VIÊN
    // ========================================

    /**
     * API Lấy dữ liệu dashboard GIÁO VIÊN
     * URL: GET /api/dashboard/giao-vien?userId=xxx
     *
     * @param userId ID của giáo viên (trong thực tế lấy từ session)
     *
     * @return ApiResponse chứa GiaoVienDashboardDTO với các thông tin:
     *         - Thông tin cá nhân
     *         - Thống kê tổng quan (số đề thi, số câu hỏi...)
     *         - Danh sách đề thi gần đây
     *         - Thống kê theo môn học
     */
    @GetMapping("/api/giao-vien")
    @ResponseBody
    public ResponseEntity<ApiResponse<GiaoVienDashboardDTO>> layDashboardGiaoVien(
            @RequestParam("userId") String userId
    ) {
        GiaoVienDashboardDTO dto = dashboardService.layDashboardGiaoVien(userId);

        if (dto == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Không tìm thấy giáo viên!", 1));
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu thành công", dto));
    }

    /**
     * API Lấy DANH SÁCH ĐỀ THI của giáo viên
     * URL: GET /api/dashboard/giao-vien/de-thi?userId=xxx
     *
     * @param userId ID của giáo viên
     *
     * @return ApiResponse chứa danh sách đề thi
     */
    @GetMapping("/api/giao-vien/de-thi")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> layDanhSachDeThi(
            @RequestParam("userId") String userId
    ) {
        // Trong thực tế sẽ gọi repository để lấy danh sách đề thi
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đề thi thành công", null));
    }

    /**
     * API Lấy THỐNG KÊ CHI TIẾT theo đề thi
     * URL: GET /api/dashboard/giao-vien/thong-ke/{deThiId}
     *
     * @param deThiId ID của đề thi
     *
     * @return ApiResponse chứa thống kê chi tiết của đề thi
     */
    @GetMapping("/api/giao-vien/thong-ke/{deThiId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> layThongKeChiTiet(
            @PathVariable("deThiId") String deThiId
    ) {
        // Trong thực tế sẽ gọi service để lấy thống kê chi tiết
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê chi tiết thành công", null));
    }

    // ========================================
    // 4. API CHUNG
    // ========================================

    /**
     * API Lấy THÔNG TIN NGƯỜI DÙNG HIỆN TẠI
     * URL: GET /api/dashboard/current-user
     *
     * Trong thực tế sẽ lấy thông tin từ Security Context/Session
     *
     * @return ApiResponse chứa thông tin người dùng đang đăng nhập
     */
    @GetMapping("/api/current-user")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> layNguoiDungHienTai() {
        // Trong thực tế sẽ lấy từ Spring Security
        // Ví dụ: Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", null));
    }

}
