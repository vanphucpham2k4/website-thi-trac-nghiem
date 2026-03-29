package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.AdminCapNhatNguoiDungDTO;
import com.example.webthitracnghiem.dto.AdminDatLaiMatKhauDTO;
import com.example.webthitracnghiem.dto.AdminDoiVaiTroDTO;
import com.example.webthitracnghiem.dto.AdminNguoiDungItemDTO;
import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.model.*;
import com.example.webthitracnghiem.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Quản lý người dùng dành cho Admin.
 */
@Service
public class AdminNguoiDungService {

    private static final int MK_TOI_THIEU = 6;

    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final VaiTroRepository vaiTroRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhienThiRepository phienThiRepository;
    private final KetQuaThiRepository ketQuaThiRepository;
    private final CauTraLoiRepository cauTraLoiRepository;
    private final DeThiRepository deThiRepository;
    private final DeThiCauHoiRepository deThiCauHoiRepository;
    private final CauHoiRepository cauHoiRepository;

    public AdminNguoiDungService(
            NguoiDungRepository nguoiDungRepository,
            NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
            VaiTroRepository vaiTroRepository,
            PasswordEncoder passwordEncoder,
            PhienThiRepository phienThiRepository,
            KetQuaThiRepository ketQuaThiRepository,
            CauTraLoiRepository cauTraLoiRepository,
            DeThiRepository deThiRepository,
            DeThiCauHoiRepository deThiCauHoiRepository,
            CauHoiRepository cauHoiRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.passwordEncoder = passwordEncoder;
        this.phienThiRepository = phienThiRepository;
        this.ketQuaThiRepository = ketQuaThiRepository;
        this.cauTraLoiRepository = cauTraLoiRepository;
        this.deThiRepository = deThiRepository;
        this.deThiCauHoiRepository = deThiCauHoiRepository;
        this.cauHoiRepository = cauHoiRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AdminNguoiDungItemDTO>> danhSachTatCa() {
        List<AdminNguoiDungItemDTO> list = nguoiDungRepository.findAll().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        return ApiResponse.success("OK", list);
    }

    @Transactional(readOnly = true)
    public ApiResponse<AdminNguoiDungItemDTO> chiTiet(String id) {
        return nguoiDungRepository.findById(id)
                .map(nd -> ApiResponse.success("OK", toItemDto(nd)))
                .orElseGet(() -> ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI));
    }

    @Transactional
    public ApiResponse<AdminNguoiDungItemDTO> capNhat(String id, AdminCapNhatNguoiDungDTO dto) {
        if (dto == null) {
            return ApiResponse.error("Dữ liệu không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        Optional<NguoiDung> opt = nguoiDungRepository.findById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        String ho = trim(dto.getHo());
        String ten = trim(dto.getTen());
        String email = trim(dto.getEmail());
        String sdt = trim(dto.getSoDienThoai());
        if (ho == null || ten == null || email == null || sdt == null) {
            return ApiResponse.error("Họ, tên, email và số điện thoại không được để trống.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (!email.contains("@")) {
            return ApiResponse.error("Email không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        NguoiDung nd = opt.get();
        Optional<NguoiDung> trungEmail = nguoiDungRepository.findByEmail(email);
        if (trungEmail.isPresent() && !trungEmail.get().getId().equals(id)) {
            return ApiResponse.error("Email đã được sử dụng.", AuthService.ERR_EMAIL_DA_TON_TAI);
        }
        Optional<NguoiDung> trungSdt = nguoiDungRepository.findBySoDienThoai(sdt);
        if (trungSdt.isPresent() && !trungSdt.get().getId().equals(id)) {
            return ApiResponse.error("Số điện thoại đã được sử dụng.", AuthService.ERR_SDT_DA_TON_TAI);
        }

        nd.setHo(ho);
        nd.setTen(ten);
        nd.setHoTen(ho + " " + ten);
        nd.setEmail(email);
        nd.setSoDienThoai(sdt);
        nguoiDungRepository.save(nd);
        return ApiResponse.success("Cập nhật thành công.", toItemDto(nd));
    }

    @Transactional
    public ApiResponse<Void> datLaiMatKhau(String id, AdminDatLaiMatKhauDTO dto) {
        if (dto == null || dto.getMatKhauMoi() == null || dto.getMatKhauMoi().trim().length() < MK_TOI_THIEU) {
            return ApiResponse.error("Mật khẩu mới phải có ít nhất " + MK_TOI_THIEU + " ký tự.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        NguoiDung nd = nguoiDungRepository.findById(id).orElse(null);
        if (nd == null) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        nd.setMatKhau(passwordEncoder.encode(dto.getMatKhauMoi().trim()));
        nguoiDungRepository.save(nd);
        return ApiResponse.success("Đã đặt lại mật khẩu.", null);
    }

    @Transactional
    public ApiResponse<AdminNguoiDungItemDTO> doiVaiTro(String id, AdminDoiVaiTroDTO dto) {
        if (dto == null || dto.getMaVaiTro() == null) {
            return ApiResponse.error("Vai trò không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        String ma = dto.getMaVaiTro().trim();
        if (!AuthService.ROLE_ADMIN.equals(ma) && !AuthService.ROLE_GIAO_VIEN.equals(ma) && !AuthService.ROLE_SINH_VIEN.equals(ma)) {
            return ApiResponse.error("Vai trò phải là ADMIN, GIAO_VIEN hoặc SINH_VIEN.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        NguoiDung nd = nguoiDungRepository.findById(id).orElse(null);
        if (nd == null) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }

        String vaiTroHienTai = layVaiTroChinh(id);
        if (AuthService.ROLE_ADMIN.equals(vaiTroHienTai) && !AuthService.ROLE_ADMIN.equals(ma)) {
            if (nguoiDungVaiTroRepository.countByVaiTro_TenVaiTro(AuthService.ROLE_ADMIN) <= 1) {
                return ApiResponse.error("Không thể đổi vai trò: đây là tài khoản quản trị duy nhất.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }

        VaiTro vt = vaiTroRepository.findByTenVaiTro(ma).orElse(null);
        if (vt == null) {
            return ApiResponse.error("Vai trò không tồn tại trong hệ thống.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }

        nguoiDungVaiTroRepository.deleteAll(nguoiDungVaiTroRepository.findByNguoiDungId(id));
        NguoiDungVaiTro moi = new NguoiDungVaiTro();
        moi.setId(UUID.randomUUID().toString());
        moi.setNguoiDung(nd);
        moi.setVaiTro(vt);
        nguoiDungVaiTroRepository.save(moi);

        return ApiResponse.success("Đã cập nhật vai trò.", toItemDto(nd));
    }

    @Transactional
    public ApiResponse<Void> xoaNguoiDung(String adminDangNhapId, String idMucTieu) {
        if (adminDangNhapId != null && adminDangNhapId.equals(idMucTieu)) {
            return ApiResponse.error("Không thể xóa chính tài khoản đang đăng nhập.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        NguoiDung nd = nguoiDungRepository.findById(idMucTieu).orElse(null);
        if (nd == null) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        if (AuthService.ROLE_ADMIN.equals(layVaiTroChinh(idMucTieu))
                && nguoiDungVaiTroRepository.countByVaiTro_TenVaiTro(AuthService.ROLE_ADMIN) <= 1) {
            return ApiResponse.error("Không thể xóa tài khoản quản trị duy nhất.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        // Phiên thi với tư cách sinh viên
        List<PhienThi> phienSinhVien = phienThiRepository.findByNguoiDung(nd);
        for (PhienThi p : phienSinhVien) {
            xoaPhienThiVaLienQuan(p);
        }

        // Đề thi do GV tạo
        List<DeThi> deThis = deThiRepository.findByNguoiDung(nd);
        for (DeThi d : deThis) {
            for (PhienThi p : phienThiRepository.findByDeThi(d)) {
                xoaPhienThiVaLienQuan(p);
            }
            deThiCauHoiRepository.deleteByDeThi(d);
            deThiRepository.delete(d);
        }

        // Câu hỏi do người dùng tạo (còn sót sau khi xóa đề)
        List<CauHoi> cauHois = cauHoiRepository.findByNguoiDung(nd);
        for (CauHoi c : cauHois) {
            deThiCauHoiRepository.deleteByCauHoi(c);
            cauHoiRepository.delete(c);
        }

        nguoiDungVaiTroRepository.deleteAll(nguoiDungVaiTroRepository.findByNguoiDungId(idMucTieu));
        nguoiDungRepository.delete(nd);
        return ApiResponse.success("Đã xóa người dùng.", null);
    }

    private void xoaPhienThiVaLienQuan(PhienThi p) {
        p.setCauHoiHienTai(null);
        phienThiRepository.save(p);
        cauTraLoiRepository.deleteByPhienThi(p);
        ketQuaThiRepository.findByPhienThi(p).ifPresent(ketQuaThiRepository::delete);
        phienThiRepository.delete(p);
    }

    private AdminNguoiDungItemDTO toItemDto(NguoiDung nd) {
        AdminNguoiDungItemDTO d = new AdminNguoiDungItemDTO();
        d.setId(nd.getId());
        d.setMaNguoiDung(nd.getMaNguoiDung());
        d.setHo(nd.getHo());
        d.setTen(nd.getTen());
        d.setHoTen(nd.getHoTen());
        d.setEmail(nd.getEmail());
        d.setSoDienThoai(nd.getSoDienThoai());
        d.setVaiTro(layVaiTroChinh(nd.getId()));
        d.setMatKhauDaBam(nd.getMatKhau() != null && nd.getMatKhau().startsWith("$2"));
        return d;
    }

    private String layVaiTroChinh(String nguoiDungId) {
        return nguoiDungVaiTroRepository.findByNguoiDungId(nguoiDungId).stream()
                .findFirst()
                .map(ndvt -> ndvt.getVaiTro().getTenVaiTro())
                .orElse("");
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
