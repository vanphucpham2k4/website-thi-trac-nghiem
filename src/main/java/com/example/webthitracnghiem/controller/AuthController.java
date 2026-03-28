package com.example.webthitracnghiem.controller;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.CaptchaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller - Xử lý các yêu cầu liên quan đến AUTHENTICATION (Xác thực)
 * Cung cấp các API endpoints cho:
 * - Trang đăng nhập
 * - Trang đăng ký
 * - API đăng nhập (AJAX)
 * - API đăng ký (AJAX)
 * - API CAPTCHA
 * - API kiểm tra email/số điện thoại đã tồn tại
 *
 * Sử dụng @Controller vì cần trả về cả view (HTML) và API (JSON)
 */
@Controller
@RequestMapping  // Request mapping mặc định cho tất cả các method trong class
public class AuthController {

    // ===== CÁC BEAN DEPENDENCY INJECTION =====
    private final AuthService authService;
    private final CaptchaService captchaService;

    /**
     * Constructor injection - Tiêm các service vào controller
     * @param authService Service xử lý đăng ký/đăng nhập
     * @param captchaService Service quản lý CAPTCHA
     */
    public AuthController(AuthService authService, CaptchaService captchaService) {
        this.authService = authService;
        this.captchaService = captchaService;
    }

    // ========================================
    // 1. TRANG VIEW (HTML)
    // ========================================

    /**
     * Trả về trang ĐĂNG NHẬP
     * URL: /login
     *
     * @return Tên template "login" -> file login.html
     */
    @GetMapping("/login")
    public String trangDangNhap() {
        // Trả về template login.html trong thư mục templates
        return "login";
    }

    /**
     * Trả về trang ĐĂNG NHẬP ADMIN
     * URL: /login/admin
     *
     * @return Tên template "login-admin" -> file login-admin.html
     */
    @GetMapping("/login/admin")
    public String trangDangNhapAdmin() {
        return "login-admin";
    }

    /**
     * Trả về trang ĐĂNG KÝ
     * URL: /register
     *
     * @return Tên template "register" -> file register.html
     */
    @GetMapping("/register")
    public String trangDangKy() {
        // Trả về template register.html trong thư mục templates
        return "register";
    }

    // ========================================
    // 2. API CAPTCHA
    // ========================================

    /**
     * API Tạo CAPTCHA mới
     * URL: GET /api/captcha
     *
     * @return JSON chứa CAPTCHA ID và câu hỏi hiển thị
     *
     * Ví dụ response:
     * {
     *   "captchaId": "abc123-uuid...",
     *   "captchaQuestion": "5 + 3 = ?"
     * }
     */
    @GetMapping("/api/captcha")
    @ResponseBody  // Trả về JSON thay vì view HTML
    public ResponseEntity<CaptchaDTO> taoCaptcha() {
        // Tạo CAPTCHA mới và trả về cho frontend
        CaptchaDTO captcha = captchaService.generateCaptcha();
        return ResponseEntity.ok(captcha);
    }

    // ========================================
    // 3. API ĐĂNG NHẬP
    // ========================================

    /**
     * API ĐĂNG NHẬP - Xử lý form đăng nhập qua AJAX
     * URL: POST /api/login
     *
     * Chỉ dành cho SINH_VIEN và GIAO_VIEN. ADMIN phải đăng nhập qua /login/admin (API /api/login/admin)
     *
     * @param dto Dữ liệu đăng nhập (tài khoản + mật khẩu)
     *
     * @return ApiResponse chứa:
     *         - success: true/false
     *         - message: thông báo kết quả
     *         - data: thông tin người dùng (nếu thành công)
     *         - errorCode: mã lỗi chi tiết (nếu thất bại)
     *
     * Mã lỗi:
     * 1 = Tài khoản không tồn tại
     * 2 = Sai mật khẩu
     * 6 = Vai trò không hợp lệ (ADMIN bị chặn)
     * 7 = Lỗi hệ thống
     */
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<ApiResponse<DangNhapResponseDTO>> dangNhap(
            @Valid @RequestBody DangNhapDTO dto
    ) {
        ApiResponse<DangNhapResponseDTO> response = authService.dangNhap(dto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * API ĐĂNG NHẬP ADMIN - Xử lý form đăng nhập Admin qua AJAX
     * URL: POST /api/login/admin
     *
     * Chỉ dành cho vai trò ADMIN. Nếu tài khoản không phải ADMIN sẽ bị từ chối.
     *
     * @param dto Dữ liệu đăng nhập (tài khoản + mật khẩu)
     *
     * @return ApiResponse chứa:
     *         - success: true/false
     *         - message: thông báo kết quả
     *         - data: thông tin người dùng (nếu thành công)
     *         - errorCode: mã lỗi chi tiết (nếu thất bại)
     */
    @PostMapping("/api/login/admin")
    @ResponseBody
    public ResponseEntity<ApiResponse<DangNhapResponseDTO>> dangNhapAdmin(
            @Valid @RequestBody DangNhapDTO dto
    ) {
        ApiResponse<DangNhapResponseDTO> response = authService.dangNhapAdmin(dto);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    // ========================================
    // 4. API ĐĂNG KÝ
    // ========================================

    /**
     * API ĐĂNG KÝ - Xử lý form đăng ký qua AJAX
     * URL: POST /api/register
     *
     * @param dto Dữ liệu đăng ký bao gồm:
     *            - email: email người dùng
     *            - ho: họ người dùng
     *            - ten: tên người dùng
     *            - soDienThoai: số điện thoại
     *            - matKhau: mật khẩu
     *            - vaiTro: vai trò muốn đăng ký (SINH_VIEN hoặc GIAO_VIEN)
     *            - captchaId: ID của CAPTCHA
     *            - captchaAnswer: đáp án CAPTCHA
     *
     * @return ApiResponse chứa:
     *         - success: true/false
     *         - message: thông báo kết quả
     *         - data: thông tin người dùng mới tạo (nếu thành công)
     *         - errorCode: mã lỗi chi tiết (nếu thất bại)
     *
     * Mã lỗi:
     * 3 = CAPTCHA sai
     * 4 = Email đã tồn tại
     * 5 = Số điện thoại đã tồn tại
     * 6 = Vai trò không hợp lệ
     * 7 = Lỗi hệ thống
     * 8 = Dữ liệu không hợp lệ
     */
    @PostMapping("/api/register")
    @ResponseBody  // Trả về JSON thay vì view HTML
    public ResponseEntity<ApiResponse<NguoiDungDTO>> dangKy(
            @Valid @RequestBody DangKyDTO dto  // @Valid để kiểm tra validation
    ) {
        // Gọi service xử lý đăng ký
        ApiResponse<NguoiDungDTO> response = authService.dangKy(dto);

        // Trả về response với HTTP status tương ứng
        if (response.isSuccess()) {
            // Đăng ký thành công -> HTTP 201 Created
            return ResponseEntity.status(201).body(response);
        } else {
            // Đăng ký thất bại -> HTTP 400 Bad Request
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ========================================
    // 5. API KIỂM TRA DỮ LIỆU (VALIDATION)
    // ========================================

    /**
     * API Kiểm tra EMAIL đã tồn tại chưa
     * URL: GET /api/check-email?email=xxx
     *
     * Dùng để frontend validate realtime khi user nhập email
     *
     * @param email Email cần kiểm tra
     * @return JSON chứa:
     *         - exists: true nếu email đã tồn tại
     *         - message: thông báo tương ứng
     */
    @GetMapping("/api/check-email")
    @ResponseBody  // Trả về JSON
    public ResponseEntity<ApiResponse<Boolean>> kiemTraEmail(
            @RequestParam("email") String email
    ) {
        boolean tonTai = authService.kiemTraEmailTonTai(email);

        if (tonTai) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Email đã được đăng ký", false, 4));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(true, "Email có thể sử dụng", true, 0));
        }
    }

    /**
     * API Kiểm tra SỐ ĐIỆN THOẠI đã tồn tại chưa
     * URL: GET /api/check-sdt?sdt=xxx
     *
     * Dùng để frontend validate realtime khi user nhập số điện thoại
     *
     * @param sdt Số điện thoại cần kiểm tra
     * @return JSON chứa:
     *         - exists: true nếu số điện thoại đã tồn tại
     *         - message: thông báo tương ứng
     */
    @GetMapping("/api/check-sdt")
    @ResponseBody  // Trả về JSON
    public ResponseEntity<ApiResponse<Boolean>> kiemTraSoDienThoai(
            @RequestParam("sdt") String sdt
    ) {
        boolean tonTai = authService.kiemTraSoDienThoaiTonTai(sdt);

        if (tonTai) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Số điện thoại đã được đăng ký", false, 5));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(true, "Số điện thoại có thể sử dụng", true, 0));
        }
    }

    /**
     * API Kiểm tra đáp án CAPTCHA
     * URL: GET /api/check-captcha?captchaId=xxx&answer=5
     *
     * Dùng để frontend validate CAPTCHA trước khi submit form
     *
     * @param captchaId ID của CAPTCHA cần kiểm tra
     * @param answer Đáp án người dùng nhập
     * @return JSON chứa:
     *         - valid: true nếu đáp án đúng
     *         - message: thông báo tương ứng
     */
    @GetMapping("/api/check-captcha")
    @ResponseBody  // Trả về JSON
    public ResponseEntity<ApiResponse<Boolean>> kiemTraCaptcha(
            @RequestParam("captchaId") String captchaId,
            @RequestParam("answer") Integer answer
    ) {
        boolean hopLe = captchaService.checkCaptchaOnly(captchaId, answer);

        if (hopLe) {
            return ResponseEntity.ok(new ApiResponse<>(true, "CAPTCHA hợp lệ", true, 0));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(true, "CAPTCHA không đúng", false, 3));
        }
    }

    // ========================================
    // 6. API ĐĂNG XUẤT
    // ========================================

    /**
     * API ĐĂNG XUẤT - Đăng khỏi hệ thống
     * URL: POST /api/logout
     *
     * @return ApiResponse thông báo đăng xuất thành công
     */
    @PostMapping("/api/logout")
    @ResponseBody  // Trả về JSON
    public ResponseEntity<ApiResponse<Void>> dangXuat() {
        // Trong thực tế, có thể cần xóa session hoặc token ở đây
        // Hiện tại chỉ trả về thành công
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công!"));
    }
}
