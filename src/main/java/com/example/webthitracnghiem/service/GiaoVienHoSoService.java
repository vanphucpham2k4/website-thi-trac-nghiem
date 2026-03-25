package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.CapNhatHoSoGiaoVienDTO;
import com.example.webthitracnghiem.dto.DoiMatKhauDTO;
import com.example.webthitracnghiem.dto.NguoiDungDTO;
import com.example.webthitracnghiem.entity.NguoiDung;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.NguoiDungVaiTroRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Hồ sơ cá nhân giáo viên: đọc/cập nhật thông tin và đổi mật khẩu.
 */
@Service
public class GiaoVienHoSoService {

    private static final int MAT_KHAU_TOI_THIEU = 6;

    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final PasswordEncoder passwordEncoder;

    public GiaoVienHoSoService(
            NguoiDungRepository nguoiDungRepository,
            NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
            PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ApiResponse<NguoiDungDTO> layHoSo(String nguoiDungId) {
        if (!laGiaoVien(nguoiDungId)) {
            return ApiResponse.error("Tài khoản không có quyền giáo viên.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }
        return nguoiDungRepository.findById(nguoiDungId)
                .map(nd -> ApiResponse.success("OK", toDto(nd)))
                .orElseGet(() -> ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI));
    }

    @Transactional
    public ApiResponse<NguoiDungDTO> capNhatHoSo(String nguoiDungId, CapNhatHoSoGiaoVienDTO dto) {
        if (!laGiaoVien(nguoiDungId)) {
            return ApiResponse.error("Tài khoản không có quyền giáo viên.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }
        if (dto == null) {
            return ApiResponse.error("Dữ liệu không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        String ho = trimToNull(dto.getHo());
        String ten = trimToNull(dto.getTen());
        String email = trimToNull(dto.getEmail());
        String soDienThoai = trimToNull(dto.getSoDienThoai());

        if (ho == null || ten == null || email == null || soDienThoai == null) {
            return ApiResponse.error("Họ, tên, email và số điện thoại không được để trống.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (!email.contains("@")) {
            return ApiResponse.error("Email không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        Optional<NguoiDung> optNd = nguoiDungRepository.findById(nguoiDungId);
        if (optNd.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        Optional<NguoiDung> trungEmail = nguoiDungRepository.findByEmail(email);
        if (trungEmail.isPresent() && !trungEmail.get().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Email đã được sử dụng bởi tài khoản khác.", AuthService.ERR_EMAIL_DA_TON_TAI);
        }

        Optional<NguoiDung> trungSdt = nguoiDungRepository.findBySoDienThoai(soDienThoai);
        if (trungSdt.isPresent() && !trungSdt.get().getId().equals(nguoiDungId)) {
            return ApiResponse.error("Số điện thoại đã được sử dụng bởi tài khoản khác.", AuthService.ERR_SDT_DA_TON_TAI);
        }

        NguoiDung nd = optNd.get();
        nd.setHo(ho);
        nd.setTen(ten);
        nd.setHoTen(ho + " " + ten);
        nd.setEmail(email);
        nd.setSoDienThoai(soDienThoai);
        nguoiDungRepository.save(nd);

        return ApiResponse.success("Cập nhật hồ sơ thành công.", toDto(nd));
    }

    @Transactional
    public ApiResponse<Void> doiMatKhau(String nguoiDungId, DoiMatKhauDTO dto) {
        if (!laGiaoVien(nguoiDungId)) {
            return ApiResponse.error("Tài khoản không có quyền giáo viên.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }
        if (dto == null || dto.getMatKhauCu() == null || dto.getMatKhauMoi() == null) {
            return ApiResponse.error("Vui lòng nhập đầy đủ mật khẩu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        String matKhauMoi = dto.getMatKhauMoi().trim();
        if (matKhauMoi.length() < MAT_KHAU_TOI_THIEU) {
            return ApiResponse.error("Mật khẩu mới phải có ít nhất " + MAT_KHAU_TOI_THIEU + " ký tự.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        NguoiDung nd = nguoiDungRepository.findById(nguoiDungId).orElse(null);
        if (nd == null) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        if (!passwordEncoder.matches(dto.getMatKhauCu(), nd.getMatKhau())) {
            return ApiResponse.error("Mật khẩu hiện tại không đúng.", AuthService.ERR_SAI_MAT_KHAU);
        }

        nd.setMatKhau(passwordEncoder.encode(matKhauMoi));
        nguoiDungRepository.save(nd);
        return ApiResponse.success("Đổi mật khẩu thành công.", null);
    }

    private boolean laGiaoVien(String nguoiDungId) {
        return nguoiDungVaiTroRepository.findByNguoiDungId(nguoiDungId).stream()
                .anyMatch(ndvt -> AuthService.ROLE_GIAO_VIEN.equals(ndvt.getVaiTro().getTenVaiTro()));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private NguoiDungDTO toDto(NguoiDung nd) {
        NguoiDungDTO d = new NguoiDungDTO();
        d.setId(nd.getId());
        d.setMaNguoiDung(nd.getMaNguoiDung());
        d.setHo(nd.getHo());
        d.setTen(nd.getTen());
        d.setHoTen(nd.getHoTen());
        d.setEmail(nd.getEmail());
        d.setSoDienThoai(nd.getSoDienThoai());
        d.setVaiTro(AuthService.ROLE_GIAO_VIEN);
        return d;
    }
}
