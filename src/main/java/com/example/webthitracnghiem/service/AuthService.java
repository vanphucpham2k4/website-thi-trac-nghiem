package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.entity.*;
import com.example.webthitracnghiem.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service - Xử lý các nghiệp vụ liên quan đến AUTHENTICATION (Xác thực)
 * Bao gồm: Đăng ký tài khoản mới, Đăng nhập, Quản lý phiên đăng nhập
 * Sử dụng @Transactional để đảm bảo tính toàn vẹn dữ liệu khi đăng ký
 */
@Service
public class AuthService {

    // ===== CÁC HẰNG SỐ MÃ LỖI =====
    // Mã lỗi được trả về qua ApiResponse để frontend xử lý theo từng trường hợp
    public static final int ERR_OK = 0;              // Thành công
    public static final int ERR_TAI_KHOAN_KHONG_TON_TAI = 1;   // Tài khoản (email/sdt) không tồn tại
    public static final int ERR_SAI_MAT_KHAU = 2;    // Mật khẩu không đúng
    public static final int ERR_CAPTCHA_SAI = 3;      // CAPTCHA nhập sai
    public static final int ERR_EMAIL_DA_TON_TAI = 4; // Email đã được đăng ký
    public static final int ERR_SDT_DA_TON_TAI = 5;  // Số điện thoại đã được đăng ký
    public static final int ERR_VAI_TRO_KHONG_HOP_LE = 6;  // Vai trò không hợp lệ
    public static final int ERR_HE_THONG = 7;         // Lỗi hệ thống không xác định
    public static final int ERR_DU_LIEU_KHONG_HOP_LE = 8;   // Dữ liệu đầu vào không hợp lệ

    // ===== CÁC HẰNG SỐ TÊN VAI TRÒ =====
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_GIAO_VIEN = "GIAO_VIEN";
    public static final String ROLE_SINH_VIEN = "SINH_VIEN";

    // ===== CÁC BEAN DEPENDENCY INJECTION =====
    // Các Repository để tương tác với database
    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    // Service CAPTCHA để xác thực người dùng là con người
    private final CaptchaService captchaService;

    // BCrypt encoder — mã hóa mật khẩu trước khi lưu, so sánh khi đăng nhập
    private final PasswordEncoder passwordEncoder;

    // JWT Service — tạo và xác thực token khi đăng nhập
    private final JwtService jwtService;

    /**
     * Constructor injection - Tiêm các dependency vào service
     * @param nguoiDungRepository Repository người dùng
     * @param vaiTroRepository Repository vai trò
     * @param nguoiDungVaiTroRepository Repository người dùng vai trò
     * @param captchaService Service CAPTCHA
     * @param passwordEncoder BCrypt encoder để mã hóa/so khớp mật khẩu
     */
    public AuthService(NguoiDungRepository nguoiDungRepository,
                      VaiTroRepository vaiTroRepository,
                      NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                      CaptchaService captchaService,
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.captchaService = captchaService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ========================================
    // 1. XỬ LÝ ĐĂNG KÝ
    // ========================================

    /**
     * Đăng ký tài khoản mới cho người dùng
     * Quy trình:
     * 1. Kiểm tra CAPTCHA (xác nhận là người thật)
     * 2. Kiểm tra email đã tồn tại chưa
     * 3. Kiểm tra số điện thoại đã tồn tại chưa
     * 4. Kiểm tra vai trò hợp lệ
     * 5. Tạo người dùng mới và gán vai trò
     *
     * @param dto Dữ liệu đăng ký từ frontend
     * @return ApiResponse chứa kết quả đăng ký
     */
    @Transactional  // Đảm bảo tất cả thao tác DB trong method này là 1 transaction
    public ApiResponse<NguoiDungDTO> dangKy(DangKyDTO dto) {
        try {
            // ===== BƯỚC 1: Kiểm tra CAPTCHA =====
            // Xác thực người dùng là con người, không phải bot
            if (!captchaService.validateCaptcha(dto.getCaptchaId(), dto.getCaptchaAnswer())) {
                return ApiResponse.error("Mã CAPTCHA không đúng! Vui lòng nhập lại.", ERR_CAPTCHA_SAI);
            }

            // ===== BƯỚC 2: Kiểm tra EMAIL đã tồn tại chưa =====
            // Mỗi email chỉ được đăng ký 1 lần
            if (nguoiDungRepository.existsByEmail(dto.getEmail())) {
                return ApiResponse.error("Email đã được đăng ký! Vui lòng sử dụng email khác.", ERR_EMAIL_DA_TON_TAI);
            }

            // ===== BƯỚC 3: Kiểm tra SỐ ĐIỆN THOẠI đã tồn tại chưa =====
            // Mỗi số điện thoại chỉ được đăng ký 1 lần
            if (nguoiDungRepository.existsBySoDienThoai(dto.getSoDienThoai())) {
                return ApiResponse.error("Số điện thoại đã được đăng ký! Vui lòng sử dụng số khác.", ERR_SDT_DA_TON_TAI);
            }

            // ===== BƯỚC 4: Kiểm tra VAI TRÒ hợp lệ =====
            // Chỉ cho phép đăng ký vai trò SINH_VIEN hoặc GIAO_VIEN
            // ADMIN không được đăng ký tự do (phải do người quản trị tạo)
            if (!dto.getVaiTro().equals(ROLE_SINH_VIEN) &&
                !dto.getVaiTro().equals(ROLE_GIAO_VIEN)) {
                return ApiResponse.error("Vai trò không hợp lệ! Chỉ có thể đăng ký là Sinh Viên hoặc Giáo Viên.", ERR_VAI_TRO_KHONG_HOP_LE);
            }

            // ===== BƯỚC 5: Tìm thông tin vai trò trong database =====
            VaiTro vaiTro = vaiTroRepository.findByTenVaiTro(dto.getVaiTro())
                    .orElse(null);

            // Nếu vai trò chưa có trong database, báo lỗi
            if (vaiTro == null) {
                return ApiResponse.error("Vai trò không tồn tại trong hệ thống! Vui lòng liên hệ quản trị viên.", ERR_VAI_TRO_KHONG_HOP_LE);
            }

            // ===== BƯỚC 6: Tạo người dùng mới =====
            NguoiDung nguoiDung = new NguoiDung();
            nguoiDung.setId(UUID.randomUUID().toString());  // Tạo ID duy nhất bằng UUID
            nguoiDung.setMaNguoiDung(taoMaNguoiDung());     // Tạo mã người dùng tự động
            nguoiDung.setHo(dto.getHo());
            nguoiDung.setTen(dto.getTen());
            nguoiDung.setHoTen(dto.getHo() + " " + dto.getTen());  // Ghép họ và tên
            nguoiDung.setEmail(dto.getEmail());
            nguoiDung.setSoDienThoai(dto.getSoDienThoai());
            nguoiDung.setMatKhau(passwordEncoder.encode(dto.getMatKhau()));  // Mã hóa BCrypt trước khi lưu vào DB

            // Lưu người dùng vào database
            nguoiDung = nguoiDungRepository.save(nguoiDung);

            // ===== BƯỚC 7: Gán vai trò cho người dùng =====
            NguoiDungVaiTro nguoiDungVaiTro = new NguoiDungVaiTro();
            nguoiDungVaiTro.setId(UUID.randomUUID().toString());
            nguoiDungVaiTro.setNguoiDung(nguoiDung);
            nguoiDungVaiTro.setVaiTro(vaiTro);
            nguoiDungVaiTroRepository.save(nguoiDungVaiTro);

            // ===== BƯỚC 8: Trả về kết quả thành công =====
            // Chuyển đổi entity sang DTO để không gửi mật khẩu về frontend
            NguoiDungDTO nguoiDungDTO = chuyenDoiNguoiDungDTO(nguoiDung, dto.getVaiTro());
            return ApiResponse.success("Đăng ký tài khoản thành công!", nguoiDungDTO);

        } catch (Exception e) {
            // Log lỗi để debug
            e.printStackTrace();
            return ApiResponse.error("Đã xảy ra lỗi trong quá trình đăng ký: " + e.getMessage(), ERR_HE_THONG);
        }
    }

    // ========================================
    // 2. XỬ LÝ ĐĂNG NHẬP
    // ========================================

    /**
     * Đăng nhập vào hệ thống
     * Quy trình:
     * 1. Kiểm tra CAPTCHA (xác nhận là người thật)
     * 2. Tìm người dùng theo email hoặc số điện thoại
     * 3. Kiểm tra mật khẩu có khớp không
     * 4. Trả về thông tin người dùng nếu thành công
     *
     * @param dto Dữ liệu đăng nhập từ frontend (tài khoản + mật khẩu + CAPTCHA)
     * @return ApiResponse chứa kết quả đăng nhập
     */
    public ApiResponse<DangNhapResponseDTO> dangNhap(DangNhapDTO dto) {
        try {
            // ===== BƯỚC 1: Kiểm tra CAPTCHA =====
            // Xác thực người dùng là con người, không phải bot
            if (!captchaService.validateCaptcha(dto.getCaptchaId(), dto.getCaptchaAnswer())) {
                return ApiResponse.error("Mã CAPTCHA không đúng! Vui lòng nhập lại.", ERR_CAPTCHA_SAI);
            }

            // ===== BƯỚC 2: Tìm người dùng theo email hoặc số điện thoại =====
            // Đăng nhập = email HOẶC số điện thoại
            NguoiDung nguoiDung = nguoiDungRepository.findByEmail(dto.getTaiKhoan())
                    .orElseGet(() -> nguoiDungRepository.findBySoDienThoai(dto.getTaiKhoan())
                            .orElse(null));

            // ===== BƯỚC 3: Kiểm tra tài khoản có tồn tại không =====
            if (nguoiDung == null) {
                // Tài khoản (email/sdt) không tồn tại trong hệ thống
                return ApiResponse.error("Tài khoản không tồn tại! Vui lòng kiểm tra lại email hoặc số điện thoại.", ERR_TAI_KHOAN_KHONG_TON_TAI);
            }

            // ===== BƯỚC 4: Kiểm tra mật khẩu (BCrypt so khớp plain vs hash) =====
            if (!passwordEncoder.matches(dto.getMatKhau(), nguoiDung.getMatKhau())) {
                // Mật khẩu không đúng
                return ApiResponse.error("Sai mật khẩu! Vui lòng nhập lại.", ERR_SAI_MAT_KHAU);
            }

            // ===== BƯỚC 5: Lấy thông tin vai trò của người dùng =====
            String vaiTro = layVaiTroCuaNguoiDung(nguoiDung.getId());

            // ===== BƯỚC 6: Tạo JWT token =====
            String token = jwtService.taoToken(nguoiDung.getId(), nguoiDung.getEmail(), vaiTro);

            // ===== BƯỚC 7: Trả về kết quả thành công (kèm token) =====
            NguoiDungDTO nguoiDungDTO = chuyenDoiNguoiDungDTO(nguoiDung, vaiTro);
            DangNhapResponseDTO responseDTO = new DangNhapResponseDTO(
                    nguoiDungDTO, token, jwtService.layThoiDiemHetHan(token)
            );
            return ApiResponse.success("Đăng nhập thành công!", responseDTO);

        } catch (Exception e) {
            // Log lỗi để debug
            e.printStackTrace();
            return ApiResponse.error("Đã xảy ra lỗi trong quá trình đăng nhập: " + e.getMessage(), ERR_HE_THONG);
        }
    }

    // ========================================
    // 3. CÁC PHƯƠNG THỨC HỖ TRỢ
    // ========================================

    /**
     * Tạo mã người dùng tự động theo format: ND + timestamp
     * Ví dụ: ND1712345678901
     * @return Mã người dùng mới
     */
    private String taoMaNguoiDung() {
        return "ND" + System.currentTimeMillis();
    }

    /**
     * Chuyển đổi entity NguoiDung sang DTO
     * Loại bỏ các thông tin nhạy cảm như mật khẩu
     *
     * @param nguoiDung Entity người dùng
     * @param vaiTro Tên vai trò của người dùng
     * @return NguoiDungDTO chỉ chứa thông tin công khai
     */
    private NguoiDungDTO chuyenDoiNguoiDungDTO(NguoiDung nguoiDung, String vaiTro) {
        NguoiDungDTO dto = new NguoiDungDTO();
        dto.setId(nguoiDung.getId());
        dto.setMaNguoiDung(nguoiDung.getMaNguoiDung());
        dto.setHo(nguoiDung.getHo());
        dto.setTen(nguoiDung.getTen());
        dto.setHoTen(nguoiDung.getHoTen());
        dto.setEmail(nguoiDung.getEmail());
        dto.setSoDienThoai(nguoiDung.getSoDienThoai());
        dto.setVaiTro(vaiTro);
        return dto;
    }

    /**
     * Lấy vai trò đầu tiên của người dùng từ bảng NGUOI_DUNG_VAI_TRO
     *
     * @param nguoiDungId ID của người dùng cần lấy vai trò
     * @return Tên vai trò hoặc null nếu không có
     */
    private String layVaiTroCuaNguoiDung(String nguoiDungId) {
        return nguoiDungVaiTroRepository.findByNguoiDungId(nguoiDungId)
                .stream()
                .findFirst()
                .map(ndvt -> ndvt.getVaiTro().getTenVaiTro())
                .orElse(null);
    }

    /**
     * Kiểm tra email đã tồn tại chưa (dùng khi frontend validate realtime)
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    public boolean kiemTraEmailTonTai(String email) {
        return nguoiDungRepository.existsByEmail(email);
    }

    /**
     * Kiểm tra số điện thoại đã tồn tại chưa (dùng khi frontend validate realtime)
     * @param soDienThoai Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã tồn tại
     */
    public boolean kiemTraSoDienThoaiTonTai(String soDienThoai) {
        return nguoiDungRepository.existsBySoDienThoai(soDienThoai);
    }
}
