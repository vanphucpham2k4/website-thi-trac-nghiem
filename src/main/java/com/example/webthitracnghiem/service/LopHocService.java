package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.GiaoVienLopHocChiTietDTO;
import com.example.webthitracnghiem.dto.GiaoVienLopHocListItemDTO;
import com.example.webthitracnghiem.dto.LopHocTaoCapNhatDTO;
import com.example.webthitracnghiem.dto.SinhVienLopPhongThiChiTietDTO;
import com.example.webthitracnghiem.dto.SinhVienLopPhongThiItemDTO;
import com.example.webthitracnghiem.model.LopHoc;
import com.example.webthitracnghiem.model.LopHocSinhVien;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.repository.LopHocRepository;
import com.example.webthitracnghiem.repository.LopHocSinhVienRepository;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lớp học: CRUD (giáo viên chủ trì) và danh sách theo sinh viên.
 */
@Service
public class LopHocService {

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final LopHocRepository lopHocRepository;
    private final LopHocSinhVienRepository lopHocSinhVienRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public LopHocService(
            LopHocRepository lopHocRepository,
            LopHocSinhVienRepository lopHocSinhVienRepository,
            NguoiDungRepository nguoiDungRepository) {
        this.lopHocRepository = lopHocRepository;
        this.lopHocSinhVienRepository = lopHocSinhVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienLopHocListItemDTO>> layDanhSachGiaoVien(String giaoVienId) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        NguoiDung gv = gvOpt.get();
        List<LopHoc> ds = lopHocRepository.findByGiaoVienOrderByThoiGianTaoDesc(gv);
        List<GiaoVienLopHocListItemDTO> out = new ArrayList<>();
        for (LopHoc l : ds) {
            out.add(sangDtoBang(gv, l));
        }
        return ApiResponse.success("OK", out);
    }

    @Transactional(readOnly = true)
    public ApiResponse<GiaoVienLopHocChiTietDTO> layChiTietGiaoVien(String giaoVienId, String lopId) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findByIdAndGiaoVien(lopId, gvOpt.get());
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp học hoặc bạn không có quyền.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();
        GiaoVienLopHocChiTietDTO dto = new GiaoVienLopHocChiTietDTO();
        dto.setId(lop.getId());
        dto.setTenLop(lop.getTen());
        List<String> ids = lopHocSinhVienRepository.findByLopHoc(lop).stream()
                .map(x -> x.getSinhVien().getId())
                .collect(Collectors.toList());
        dto.setSinhVienIds(ids);
        return ApiResponse.success("OK", dto);
    }

    @Transactional
    public ApiResponse<GiaoVienLopHocListItemDTO> taoLop(String giaoVienId, LopHocTaoCapNhatDTO dto) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        String ten = dto != null && dto.getTenLop() != null ? dto.getTenLop().trim() : "";
        if (ten.isEmpty()) {
            return ApiResponse.error("Tên lớp không được để trống.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (ten.length() > 200) {
            return ApiResponse.error("Tên lớp tối đa 200 ký tự.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        NguoiDung gv = gvOpt.get();
        ApiResponse<List<NguoiDung>> val = kiemTraVaLaySinhVien(dto != null ? dto.getSinhVienIds() : List.of());
        if (!val.isSuccess()) {
            return ApiResponse.error(val.getMessage(), val.getErrorCode());
        }
        List<NguoiDung> sinhViens = val.getData();

        LopHoc lop = new LopHoc();
        lop.setId(UUID.randomUUID().toString());
        lop.setTen(ten);
        lop.setGiaoVien(gv);
        lop.setThoiGianTao(LocalDateTime.now());
        lopHocRepository.save(lop);

        luuThanhVien(lop, sinhViens);

        return ApiResponse.success("Tạo lớp thành công.", sangDtoBang(gv, lop));
    }

    @Transactional
    public ApiResponse<GiaoVienLopHocListItemDTO> capNhatLop(String giaoVienId, String lopId, LopHocTaoCapNhatDTO dto) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        String ten = dto != null && dto.getTenLop() != null ? dto.getTenLop().trim() : "";
        if (ten.isEmpty()) {
            return ApiResponse.error("Tên lớp không được để trống.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (ten.length() > 200) {
            return ApiResponse.error("Tên lớp tối đa 200 ký tự.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findByIdAndGiaoVien(lopId, gvOpt.get());
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp học hoặc bạn không có quyền.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();
        ApiResponse<List<NguoiDung>> val = kiemTraVaLaySinhVien(dto != null ? dto.getSinhVienIds() : List.of());
        if (!val.isSuccess()) {
            return ApiResponse.error(val.getMessage(), val.getErrorCode());
        }
        List<NguoiDung> sinhViens = val.getData();

        lop.setTen(ten);
        lopHocRepository.save(lop);
        lopHocSinhVienRepository.deleteByLopHoc(lop);
        luuThanhVien(lop, sinhViens);

        return ApiResponse.success("Cập nhật lớp thành công.", sangDtoBang(gvOpt.get(), lop));
    }

    @Transactional
    public ApiResponse<Void> xoaLop(String giaoVienId, String lopId) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findByIdAndGiaoVien(lopId, gvOpt.get());
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp học hoặc bạn không có quyền.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();
        lopHocSinhVienRepository.deleteByLopHoc(lop);
        lopHocRepository.delete(lop);
        return ApiResponse.success("Đã xóa lớp học.", null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SinhVienLopPhongThiItemDTO>> layDanhSachChoSinhVien(String sinhVienId) {
        if (sinhVienId == null || sinhVienId.isBlank()) {
            return ApiResponse.error("Không xác định được sinh viên.", AuthService.ERR_HE_THONG);
        }
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        List<LopHocSinhVien> lienKet = lopHocSinhVienRepository.findBySinhVien(svOpt.get());
        List<SinhVienLopPhongThiItemDTO> out = new ArrayList<>();
        for (LopHocSinhVien lk : lienKet) {
            LopHoc l = lk.getLopHoc();
            NguoiDung gv = l.getGiaoVien();
            SinhVienLopPhongThiItemDTO row = new SinhVienLopPhongThiItemDTO();
            row.setLopId(l.getId());
            row.setTenLop(l.getTen());
            row.setTenChuTri(formatHoTen(gv));
            out.add(row);
        }
        out.sort(Comparator.comparing(SinhVienLopPhongThiItemDTO::getTenLop,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return ApiResponse.success("OK", out);
    }

    @Transactional(readOnly = true)
    public ApiResponse<SinhVienLopPhongThiChiTietDTO> layChiTietChoSinhVien(String sinhVienId, String lopId) {
        if (sinhVienId == null || sinhVienId.isBlank()) {
            return ApiResponse.error("Không xác định được sinh viên.", AuthService.ERR_HE_THONG);
        }
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findById(lopId);
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp học.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();
        if (!lopHocSinhVienRepository.existsByLopHocAndSinhVien(lop, svOpt.get())) {
            return ApiResponse.error("Bạn không thuộc lớp này.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        SinhVienLopPhongThiChiTietDTO dto = new SinhVienLopPhongThiChiTietDTO();
        dto.setLopId(lop.getId());
        dto.setTenLop(lop.getTen());
        dto.setTenChuTri(formatHoTen(lop.getGiaoVien()));
        return ApiResponse.success("OK", dto);
    }

    private void luuThanhVien(LopHoc lop, List<NguoiDung> sinhViens) {
        for (NguoiDung sv : sinhViens) {
            LopHocSinhVien lk = new LopHocSinhVien();
            lk.setId(UUID.randomUUID().toString());
            lk.setLopHoc(lop);
            lk.setSinhVien(sv);
            lopHocSinhVienRepository.save(lk);
        }
    }

    /**
     * Kiểm tra mọi id là sinh viên hợp lệ; trả về danh sách không trùng, giữ thứ tự gần như đầu vào.
     */
    private ApiResponse<List<NguoiDung>> kiemTraVaLaySinhVien(List<String> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) {
            return ApiResponse.success("OK", List.of());
        }
        Set<String> hopLe = nguoiDungRepository.findByVaiTro(AuthService.ROLE_SINH_VIEN).stream()
                .map(NguoiDung::getId)
                .collect(Collectors.toSet());
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String id : rawIds) {
            if (id != null && !id.isBlank()) {
                unique.add(id.trim());
            }
        }
        for (String id : unique) {
            if (!hopLe.contains(id)) {
                return ApiResponse.error("Có sinh viên không hợp lệ hoặc không tồn tại.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }
        List<NguoiDung> out = new ArrayList<>();
        for (String id : unique) {
            nguoiDungRepository.findById(id).ifPresent(out::add);
        }
        return ApiResponse.success("OK", out);
    }

    private GiaoVienLopHocListItemDTO sangDtoBang(NguoiDung giaoVien, LopHoc lop) {
        GiaoVienLopHocListItemDTO d = new GiaoVienLopHocListItemDTO();
        d.setId(lop.getId());
        d.setTenLop(lop.getTen());
        d.setSoSinhVien(lopHocSinhVienRepository.countByLopHoc(lop));
        d.setTenChuTri(formatHoTen(giaoVien));
        if (lop.getThoiGianTao() != null) {
            d.setThoiGianTao(lop.getThoiGianTao().format(ISO_DT));
        }
        return d;
    }

    private static String formatHoTen(NguoiDung n) {
        if (n == null) {
            return "";
        }
        if (n.getHoTen() != null && !n.getHoTen().isBlank()) {
            return n.getHoTen().trim();
        }
        return (Objects.toString(n.getHo(), "") + " " + Objects.toString(n.getTen(), "")).trim();
    }
}
