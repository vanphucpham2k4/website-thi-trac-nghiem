package com.example.webthitracnghiem.config;

import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.NguoiDungVaiTro;
import com.example.webthitracnghiem.model.VaiTro;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.NguoiDungVaiTroRepository;
import com.example.webthitracnghiem.repository.VaiTroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * DataInitializer - Khởi tạo dữ liệu mặc định khi ứng dụng bắt đầu
 * Chạy một lần sau khi Spring Boot khởi động xong
 * Tự động tạo:
 *   1. Các vai trò mặc định (ADMIN, GIAO_VIEN, SINH_VIEN)
 *   2. Tài khoản admin mặc định: admin@gmail.com / 123456
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final VaiTroRepository vaiTroRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection
     */
    public DataInitializer(VaiTroRepository vaiTroRepository,
                          NguoiDungRepository nguoiDungRepository,
                          NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                          PasswordEncoder passwordEncoder) {
        this.vaiTroRepository = vaiTroRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // ========================================
        // 1. TẠO VAI TRÒ MẶC ĐỊNH
        // ========================================
        List<VaiTroData> vaiTroList = Arrays.asList(
            new VaiTroData("ADMIN",       "Quản trị viên",  "Người quản lý toàn bộ hệ thống"),
            new VaiTroData("GIAO_VIEN",   "Giáo viên",      "Giáo viên tạo và quản lý đề thi"),
            new VaiTroData("SINH_VIEN",   "Sinh viên",      "Học sinh/Sinh viên tham gia thi trắc nghiệm")
        );

        for (VaiTroData data : vaiTroList) {
            if (!vaiTroRepository.existsByTenVaiTro(data.tenVaiTro)) {
                VaiTro vaiTro = new VaiTro();
                vaiTro.setId(UUID.randomUUID().toString());
                vaiTro.setTenVaiTro(data.tenVaiTro);
                vaiTro.setMoTa(data.moTa);
                vaiTroRepository.save(vaiTro);
                System.out.println("[DataInitializer] Đã tạo vai trò: " + data.tenVaiTro);
            } else {
                System.out.println("[DataInitializer] Vai trò đã tồn tại: " + data.tenVaiTro);
            }
        }

        // ========================================
        // 2. TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH
        // ========================================
        taoTaiKhoanAdminMacDinh();

        System.out.println("[DataInitializer] Khởi tạo dữ liệu hoàn tất!");
    }

    /**
     * Tạo tài khoản admin mặc định: admin@gmail.com / 123456
     */
    private void taoTaiKhoanAdminMacDinh() {
        final String EMAIL_ADMIN = "admin@gmail.com";
        final String MAT_KHAU  = "123456";

        // Kiểm tra đã tồn tại chưa
        if (nguoiDungRepository.existsByEmail(EMAIL_ADMIN)) {
            System.out.println("[DataInitializer] Tài khoản admin đã tồn tại: " + EMAIL_ADMIN);
            return;
        }

        // Lấy vai trò ADMIN
        VaiTro vaiTroAdmin = vaiTroRepository.findByTenVaiTro("ADMIN").orElse(null);
        if (vaiTroAdmin == null) {
            System.err.println("[DataInitializer] LỖI: Vai trò ADMIN chưa được tạo!");
            return;
        }

        // Tạo người dùng admin
        NguoiDung admin = new NguoiDung();
        admin.setId(UUID.randomUUID().toString());
        admin.setMaNguoiDung("ADMIN001");
        admin.setHo("Quản");
        admin.setTen("Trị Viên");
        admin.setHoTen("Quản Trị Viên");
        admin.setEmail(EMAIL_ADMIN);
        admin.setSoDienThoai("0000000000");
        admin.setMatKhau(passwordEncoder.encode(MAT_KHAU));  // Mã hóa BCrypt trước khi lưu
        admin = nguoiDungRepository.save(admin);

        // Gán vai trò ADMIN cho người dùng
        NguoiDungVaiTro ndvt = new NguoiDungVaiTro();
        ndvt.setId(UUID.randomUUID().toString());
        ndvt.setNguoiDung(admin);
        ndvt.setVaiTro(vaiTroAdmin);
        nguoiDungVaiTroRepository.save(ndvt);

        System.out.println("[DataInitializer] ✅ Đã tạo tài khoản admin mặc định:");
        System.out.println("[DataInitializer]    Email:    " + EMAIL_ADMIN);
        System.out.println("[DataInitializer]    Mật khẩu: " + MAT_KHAU);
    }

    private static class VaiTroData {
        String tenVaiTro;
        String moTa;
        VaiTroData(String tenVaiTro, String tenHienThi, String moTaChiTiet) {
            this.tenVaiTro = tenVaiTro;
            this.moTa = tenHienThi + " — " + moTaChiTiet;
        }
    }
}
