package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.AdminCapNhatYeuCauDoiThuongDTO;
import com.example.webthitracnghiem.dto.AdminYeuCauDoiThuongChiTietDTO;
import com.example.webthitracnghiem.dto.AdminYeuCauDoiThuongItemDTO;
import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.PhanThuong;
import com.example.webthitracnghiem.model.TrangThaiYeuCauDoiThuong;
import com.example.webthitracnghiem.model.YeuCauDoiThuong;
import com.example.webthitracnghiem.repository.PhanThuongRepository;
import com.example.webthitracnghiem.repository.YeuCauDoiThuongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AdminDoiThuongService {

    private final YeuCauDoiThuongRepository yeuCauDoiThuongRepository;
    private final PhanThuongRepository phanThuongRepository;

    public AdminDoiThuongService(
            YeuCauDoiThuongRepository yeuCauDoiThuongRepository,
            PhanThuongRepository phanThuongRepository) {
        this.yeuCauDoiThuongRepository = yeuCauDoiThuongRepository;
        this.phanThuongRepository = phanThuongRepository;
    }

    public ApiResponse<List<AdminYeuCauDoiThuongItemDTO>> danhSach(String trangThaiLoc, String q) {
        List<YeuCauDoiThuong> all = yeuCauDoiThuongRepository.findAllForAdminOrderByThoiGianDesc();
        TrangThaiYeuCauDoiThuong loc = parseTrangThaiAdmin(trangThaiLoc);
        String qn = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<AdminYeuCauDoiThuongItemDTO> out = new ArrayList<>();
        for (YeuCauDoiThuong y : all) {
            if (loc != null && y.getTrangThai() != loc) {
                continue;
            }
            if (!qn.isEmpty() && !khopTimKiem(y, qn)) {
                continue;
            }
            out.add(toItem(y));
        }
        return ApiResponse.success("OK", out);
    }

    public ApiResponse<AdminYeuCauDoiThuongChiTietDTO> chiTiet(String id) {
        if (id == null || id.isBlank()) {
            return ApiResponse.error("Thiếu mã yêu cầu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        return yeuCauDoiThuongRepository.findByIdForAdmin(id)
                .map(y -> ApiResponse.success("OK", toChiTiet(y)))
                .orElseGet(() -> ApiResponse.error("Không tìm thấy yêu cầu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE));
    }

    @Transactional
    public ApiResponse<AdminYeuCauDoiThuongChiTietDTO> capNhat(String id, AdminCapNhatYeuCauDoiThuongDTO dto) {
        if (id == null || id.isBlank()) {
            return ApiResponse.error("Thiếu mã yêu cầu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (dto == null || dto.getTrangThai() == null || dto.getTrangThai().isBlank()) {
            return ApiResponse.error("Thiếu trạng thái mới.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        YeuCauDoiThuong y = yeuCauDoiThuongRepository.findByIdForAdmin(id).orElse(null);
        if (y == null) {
            return ApiResponse.error("Không tìm thấy yêu cầu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        TrangThaiYeuCauDoiThuong moi;
        try {
            moi = TrangThaiYeuCauDoiThuong.valueOf(dto.getTrangThai().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Trạng thái không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        TrangThaiYeuCauDoiThuong cu = y.getTrangThai();
        if (cu == TrangThaiYeuCauDoiThuong.DA_HUY) {
            return ApiResponse.error("Không cập nhật yêu cầu đã hủy.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (cu == moi) {
            if (dto.getGhiChu() != null) {
                y.setGhiChu(dto.getGhiChu());
            }
            yeuCauDoiThuongRepository.save(y);
            return ApiResponse.success("Đã cập nhật.", toChiTiet(refetch(id)));
        }
        if (!chuyenTrangThaiHopLe(cu, moi)) {
            return ApiResponse.error("Chuyển trạng thái không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (moi == TrangThaiYeuCauDoiThuong.DA_HUY && canHoanTraKho(cu)) {
            hoanTraPhanThuong(y.getPhanThuong());
        }
        y.setTrangThai(moi);
        if (dto.getGhiChu() != null) {
            y.setGhiChu(dto.getGhiChu());
        }
        yeuCauDoiThuongRepository.save(y);
        return ApiResponse.success("Đã cập nhật trạng thái.", toChiTiet(refetch(id)));
    }

    @Transactional
    public ApiResponse<Void> xoa(String id) {
        if (id == null || id.isBlank()) {
            return ApiResponse.error("Thiếu mã yêu cầu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        YeuCauDoiThuong y = yeuCauDoiThuongRepository.findByIdForAdmin(id).orElse(null);
        if (y == null) {
            return ApiResponse.error("Không tìm thấy yêu cầu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        return switch (y.getTrangThai()) {
            case DA_NHAN_QUA -> ApiResponse.error("Không xóa yêu cầu đã nhận quà.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            case CHO_DUYET, DA_DUYET -> {
                hoanTraPhanThuong(y.getPhanThuong());
                yeuCauDoiThuongRepository.delete(y);
                yield ApiResponse.success("Đã xóa yêu cầu.", null);
            }
            case DA_HUY -> {
                yeuCauDoiThuongRepository.delete(y);
                yield ApiResponse.success("Đã xóa yêu cầu.", null);
            }
        };
    }

    private YeuCauDoiThuong refetch(String id) {
        return yeuCauDoiThuongRepository.findByIdForAdmin(id).orElseThrow();
    }

    private static boolean canHoanTraKho(TrangThaiYeuCauDoiThuong cu) {
        return cu == TrangThaiYeuCauDoiThuong.CHO_DUYET || cu == TrangThaiYeuCauDoiThuong.DA_DUYET;
    }

    private static boolean chuyenTrangThaiHopLe(TrangThaiYeuCauDoiThuong a, TrangThaiYeuCauDoiThuong b) {
        if (a == TrangThaiYeuCauDoiThuong.CHO_DUYET) {
            return b == TrangThaiYeuCauDoiThuong.DA_DUYET
                    || b == TrangThaiYeuCauDoiThuong.DA_NHAN_QUA
                    || b == TrangThaiYeuCauDoiThuong.DA_HUY;
        }
        if (a == TrangThaiYeuCauDoiThuong.DA_DUYET) {
            return b == TrangThaiYeuCauDoiThuong.DA_NHAN_QUA || b == TrangThaiYeuCauDoiThuong.DA_HUY;
        }
        return false;
    }

    private void hoanTraPhanThuong(PhanThuong pt) {
        pt.setSoLuongConLai(pt.getSoLuongConLai() + 1);
        phanThuongRepository.save(pt);
    }

    private static TrangThaiYeuCauDoiThuong parseTrangThaiAdmin(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String x = s.trim().toUpperCase(Locale.ROOT);
        if ("TAT_CA".equals(x)) {
            return null;
        }
        try {
            return TrangThaiYeuCauDoiThuong.valueOf(x);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean khopTimKiem(YeuCauDoiThuong y, String qn) {
        NguoiDung nd = y.getNguoiDung();
        String ma = y.getMaDoi() != null ? y.getMaDoi().toLowerCase(Locale.ROOT) : "";
        String ten = y.getPhanThuong().getTen() != null ? y.getPhanThuong().getTen().toLowerCase(Locale.ROOT) : "";
        String ht = nd.getHoTen() != null ? nd.getHoTen().toLowerCase(Locale.ROOT) : "";
        String mail = nd.getEmail() != null ? nd.getEmail().toLowerCase(Locale.ROOT) : "";
        String maNd = nd.getMaNguoiDung() != null ? nd.getMaNguoiDung().toLowerCase(Locale.ROOT) : "";
        return ma.contains(qn) || ten.contains(qn) || ht.contains(qn) || mail.contains(qn) || maNd.contains(qn);
    }

    private static AdminYeuCauDoiThuongItemDTO toItem(YeuCauDoiThuong y) {
        NguoiDung nd = y.getNguoiDung();
        AdminYeuCauDoiThuongItemDTO d = new AdminYeuCauDoiThuongItemDTO();
        d.setId(y.getId());
        d.setMaDoi(y.getMaDoi());
        d.setTrangThai(y.getTrangThai().name());
        d.setThoiGian(y.getThoiGian().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        d.setDiemDaDung(y.getDiemDaDung());
        d.setTenPhanThuong(y.getPhanThuong().getTen());
        d.setPhanThuongId(y.getPhanThuong().getId());
        d.setSinhVienId(nd.getId());
        d.setSinhVienMa(nd.getMaNguoiDung());
        d.setSinhVienHoTen(nd.getHoTen());
        d.setSinhVienEmail(nd.getEmail());
        d.setSinhVienSoDienThoai(nd.getSoDienThoai());
        return d;
    }

    private static AdminYeuCauDoiThuongChiTietDTO toChiTiet(YeuCauDoiThuong y) {
        NguoiDung nd = y.getNguoiDung();
        PhanThuong pt = y.getPhanThuong();
        AdminYeuCauDoiThuongChiTietDTO d = new AdminYeuCauDoiThuongChiTietDTO();
        d.setId(y.getId());
        d.setMaDoi(y.getMaDoi());
        d.setTrangThai(y.getTrangThai().name());
        d.setThoiGian(y.getThoiGian().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        d.setDiemDaDung(y.getDiemDaDung());
        d.setGhiChu(y.getGhiChu());
        d.setPhanThuongId(pt.getId());
        d.setTenPhanThuong(pt.getTen());
        d.setMoTaPhanThuong(pt.getMoTaNgan());
        d.setLoaiPhanThuong(pt.getLoai().name());
        d.setSinhVienId(nd.getId());
        d.setSinhVienMa(nd.getMaNguoiDung());
        d.setSinhVienHoTen(nd.getHoTen());
        d.setSinhVienEmail(nd.getEmail());
        d.setSinhVienSoDienThoai(nd.getSoDienThoai());
        d.setSinhVienDiemThuongTichLuy(nd.getDiemThuongTichLuy());
        return d;
    }
}
