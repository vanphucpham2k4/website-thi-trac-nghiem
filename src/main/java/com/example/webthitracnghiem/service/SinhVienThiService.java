package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.*;
import com.example.webthitracnghiem.model.*;
import com.example.webthitracnghiem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sinh viên: đề xuất bản theo lớp, làm bài, lưu đáp án, nộp bài, lịch sử.
 */
@Service
public class SinhVienThiService {

    public static final String TT_DANG_THI = "DANG_THI";
    public static final String TT_DA_NOP = "DA_NOP_BAI";
    public static final String TTL_DUNG = "DUNG";
    public static final String TTL_SAI = "SAI";
    public static final String TTL_CHUA_TRA_LOI = "CHUA_TRA_LOI";

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final NguoiDungRepository nguoiDungRepository;
    private final LopHocRepository lopHocRepository;
    private final LopHocSinhVienRepository lopHocSinhVienRepository;
    private final DeThiLopHocRepository deThiLopHocRepository;
    private final DeThiRepository deThiRepository;
    private final DeThiCauHoiRepository deThiCauHoiRepository;
    private final PhienThiRepository phienThiRepository;
    private final CauTraLoiRepository cauTraLoiRepository;
    private final KetQuaThiRepository ketQuaThiRepository;
    private final KiemTraLuotThiService kiemTraLuotThiService;
    private final CauHoiRepository cauHoiRepository;
    private final JwtService jwtService;

    public SinhVienThiService(
            NguoiDungRepository nguoiDungRepository,
            LopHocRepository lopHocRepository,
            LopHocSinhVienRepository lopHocSinhVienRepository,
            DeThiLopHocRepository deThiLopHocRepository,
            DeThiRepository deThiRepository,
            DeThiCauHoiRepository deThiCauHoiRepository,
            PhienThiRepository phienThiRepository,
            CauTraLoiRepository cauTraLoiRepository,
            KetQuaThiRepository ketQuaThiRepository,
            KiemTraLuotThiService kiemTraLuotThiService,
            CauHoiRepository cauHoiRepository,
            JwtService jwtService) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.lopHocRepository = lopHocRepository;
        this.lopHocSinhVienRepository = lopHocSinhVienRepository;
        this.deThiLopHocRepository = deThiLopHocRepository;
        this.deThiRepository = deThiRepository;
        this.deThiCauHoiRepository = deThiCauHoiRepository;
        this.phienThiRepository = phienThiRepository;
        this.cauTraLoiRepository = cauTraLoiRepository;
        this.ketQuaThiRepository = ketQuaThiRepository;
        this.kiemTraLuotThiService = kiemTraLuotThiService;
        this.cauHoiRepository = cauHoiRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SinhVienDeThiTrongLopDTO>> layDeThiTrongLop(String sinhVienId, String lopId) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findById(lopId);
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();
        if (!lopHocSinhVienRepository.existsByLopHocAndSinhVien(lop, svOpt.get())) {
            return ApiResponse.error("Bạn không thuộc lớp này.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        List<DeThiLopHoc> links = deThiLopHocRepository.findByLopHocOrderByThoiGianXuatBanDesc(lop);
        List<SinhVienDeThiTrongLopDTO> out = new ArrayList<>();
        NguoiDung sv = svOpt.get();
        for (DeThiLopHoc lk : links) {
            DeThi d = lk.getDeThi();
            if (d.getDeletedAt() != null) {
                continue;
            }
            if (!"CONG_KHAI".equals(d.getTrangThai())) {
                continue;
            }
            SinhVienDeThiTrongLopDTO row = new SinhVienDeThiTrongLopDTO();
            row.setDeThiId(d.getId());
            row.setTenDeThi(d.getTen());
            row.setMaDeThi(d.getMaDeThi());
            row.setThoiGianPhut(d.getThoiGianPhut());
            row.setSoCauHoi(deThiCauHoiRepository.countByDeThi(d));
            row.setTenMonHoc(d.getMonHoc() != null ? d.getMonHoc().getTen() : "");
            if (lk.getThoiGianXuatBan() != null) {
                row.setThoiGianXuatBan(lk.getThoiGianXuatBan().format(ISO_DT));
            }
            Optional<PhienThi> dangDo = phienThiRepository.timPhienDangDo(sv, d);
            if (dangDo.isPresent()) {
                row.setCoPhienDangLam(true);
                row.setPhienThiIdDangLam(dangDo.get().getId());
            } else {
                row.setCoPhienDangLam(false);
                row.setPhienThiIdDangLam(null);
            }
            out.add(row);
        }
        return ApiResponse.success("OK", out);
    }

    @Transactional
    public ApiResponse<SinhVienBatDauThiResponseDTO> batDauThi(String sinhVienId, String lopId, String deThiId) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        NguoiDung sv = svOpt.get();
        Optional<LopHoc> lopOpt = lopHocRepository.findById(lopId);
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();
        if (!lopHocSinhVienRepository.existsByLopHocAndSinhVien(lop, sv)) {
            return ApiResponse.error("Bạn không thuộc lớp này.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        Optional<DeThi> deOpt = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (deOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = deOpt.get();
        if (!"CONG_KHAI".equals(deThi.getTrangThai())) {
            return ApiResponse.error("Đề thi chưa công khai.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (!deThiLopHocRepository.existsByDeThiAndLopHoc(deThi, lop)) {
            return ApiResponse.error("Đề thi chưa được xuất bản cho lớp này.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (deThiCauHoiRepository.countByDeThi(deThi) <= 0) {
            return ApiResponse.error("Đề thi chưa có câu hỏi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (!hopLeThoiGianMoDong(deThi)) {
            return ApiResponse.error("Đề thi hiện không trong khung giờ cho phép làm bài.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        KiemTraLuotThiService.KetQuaLuotThi luot = kiemTraLuotThiService.kiemTra(sinhVienId, deThiId);
        SinhVienBatDauThiResponseDTO dto = new SinhVienBatDauThiResponseDTO();
        switch (luot.loai()) {
            case IN_PROGRESS -> {
                dto.setPhienThiId(luot.phienThiId());
                dto.setTiepTucPhienCu(true);
                return ApiResponse.success("Tiếp tục phiên làm bài.", dto);
            }
            case LIMIT_REACHED -> {
                return ApiResponse.error(luot.thongBao(), AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
            case NOT_FOUND, ERROR -> {
                return ApiResponse.error(
                        luot.thongBao() != null ? luot.thongBao() : "Không thể bắt đầu thi.",
                        AuthService.ERR_HE_THONG);
            }
            case ALLOWED -> {
                PhienThi p = new PhienThi();
                p.setId(UUID.randomUUID().toString());
                p.setDeThi(deThi);
                p.setNguoiDung(sv);
                p.setThoiGianBatDau(LocalDateTime.now());
                p.setTrangThai(TT_DANG_THI);
                p.setLopHoc(lop);
                phienThiRepository.save(p);
                dto.setPhienThiId(p.getId());
                dto.setTiepTucPhienCu(false);
                return ApiResponse.success("Đã bắt đầu làm bài.", dto);
            }
        }
        return ApiResponse.error("Không thể bắt đầu thi.", AuthService.ERR_HE_THONG);
    }

    /**
     * Bắt đầu / tiếp tục làm bài qua link công khai (mã {@link DeThi#getMaTruyCap()}), không cần lớp.
     */
    @Transactional
    public ApiResponse<SinhVienBatDauThiResponseDTO> batDauThiQuaLinkCongKhai(String sinhVienId, String maTruyCap) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        NguoiDung sv = svOpt.get();
        if (maTruyCap == null || maTruyCap.isBlank()) {
            return ApiResponse.error("Mã tham gia không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        Optional<DeThi> deOpt = deThiRepository.findByMaTruyCapAndDeletedAtIsNull(maTruyCap.trim());
        if (deOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi hoặc link đã bị thu hồi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        DeThi deThi = deOpt.get();
        if (!"CONG_KHAI".equals(deThi.getTrangThai())) {
            return ApiResponse.error("Đề thi chưa công khai.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (deThiCauHoiRepository.countByDeThi(deThi) <= 0) {
            return ApiResponse.error("Đề thi chưa có câu hỏi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (!hopLeThoiGianMoDong(deThi)) {
            return ApiResponse.error("Đề thi hiện không trong khung giờ cho phép làm bài.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        KiemTraLuotThiService.KetQuaLuotThi luot = kiemTraLuotThiService.kiemTra(sinhVienId, deThi.getId());
        SinhVienBatDauThiResponseDTO dto = new SinhVienBatDauThiResponseDTO();
        switch (luot.loai()) {
            case IN_PROGRESS -> {
                dto.setPhienThiId(luot.phienThiId());
                dto.setTiepTucPhienCu(true);
                return ApiResponse.success("Tiếp tục phiên làm bài.", dto);
            }
            case LIMIT_REACHED -> {
                return ApiResponse.error(luot.thongBao(), AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
            case NOT_FOUND, ERROR -> {
                return ApiResponse.error(
                        luot.thongBao() != null ? luot.thongBao() : "Không thể bắt đầu thi.",
                        AuthService.ERR_HE_THONG);
            }
            case ALLOWED -> {
                PhienThi p = new PhienThi();
                p.setId(UUID.randomUUID().toString());
                p.setDeThi(deThi);
                p.setNguoiDung(sv);
                p.setThoiGianBatDau(LocalDateTime.now());
                p.setTrangThai(TT_DANG_THI);
                p.setLopHoc(null);
                phienThiRepository.save(p);
                dto.setPhienThiId(p.getId());
                dto.setTiepTucPhienCu(false);
                return ApiResponse.success("Đã bắt đầu làm bài.", dto);
            }
        }
        return ApiResponse.error("Không thể bắt đầu thi.", AuthService.ERR_HE_THONG);
    }

    /**
     * Bắt đầu / tiếp tục thi ẩn danh qua link (chỉ họ tên, không tài khoản).
     */
    @Transactional
    public ApiResponse<BatDauThiAnDanhResponseDTO> batDauThiAnDanhQuaLink(String maTruyCap, String hoTenRaw) {
        String hoTen = chuanHoaHoTenAnDanh(hoTenRaw);
        if (hoTen.length() < 2 || hoTen.length() > 200) {
            return ApiResponse.error("Vui lòng nhập họ và tên (2–200 ký tự).", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (maTruyCap == null || maTruyCap.isBlank()) {
            return ApiResponse.error("Mã tham gia không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        Optional<DeThi> deOpt = deThiRepository.findByMaTruyCapAndDeletedAtIsNull(maTruyCap.trim());
        if (deOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi hoặc link đã bị thu hồi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        DeThi deThi = deOpt.get();
        if (!"CONG_KHAI".equals(deThi.getTrangThai())) {
            return ApiResponse.error("Đề thi chưa công khai.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (deThiCauHoiRepository.countByDeThi(deThi) <= 0) {
            return ApiResponse.error("Đề thi chưa có câu hỏi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (!hopLeThoiGianMoDong(deThi)) {
            return ApiResponse.error("Đề thi hiện không trong khung giờ cho phép làm bài.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        KiemTraLuotThiService.KetQuaLuotThi luot = kiemTraLuotThiService.kiemTraAnDanh(deThi.getId(), hoTen);
        BatDauThiAnDanhResponseDTO out = new BatDauThiAnDanhResponseDTO();
        switch (luot.loai()) {
            case IN_PROGRESS -> {
                String token = jwtService.taoTokenThiAnDanh(luot.phienThiId());
                out.setPhienThiId(luot.phienThiId());
                out.setLamBaiToken(token);
                out.setExpiresAt(jwtService.layThoiDiemHetHan(token));
                return ApiResponse.success("Tiếp tục phiên làm bài.", out);
            }
            case LIMIT_REACHED -> {
                return ApiResponse.error(luot.thongBao(), AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
            case NOT_FOUND, ERROR -> {
                return ApiResponse.error(
                        luot.thongBao() != null ? luot.thongBao() : "Không thể bắt đầu thi.",
                        AuthService.ERR_HE_THONG);
            }
            case ALLOWED -> {
                PhienThi p = new PhienThi();
                p.setId(UUID.randomUUID().toString());
                p.setDeThi(deThi);
                p.setNguoiDung(null);
                p.setHoTenAnDanh(hoTen);
                p.setThoiGianBatDau(LocalDateTime.now());
                p.setTrangThai(TT_DANG_THI);
                p.setLopHoc(null);
                phienThiRepository.save(p);
                String token = jwtService.taoTokenThiAnDanh(p.getId());
                out.setPhienThiId(p.getId());
                out.setLamBaiToken(token);
                out.setExpiresAt(jwtService.layThoiDiemHetHan(token));
                return ApiResponse.success("Đã bắt đầu làm bài.", out);
            }
        }
        return ApiResponse.error("Không thể bắt đầu thi.", AuthService.ERR_HE_THONG);
    }

    @Transactional(readOnly = true)
    public ApiResponse<SinhVienBaiThiDTO> layNoiDungBaiThiAnDanh(String phienThiId) {
        Optional<PhienThi> pOpt = timPhienThiAnDanh(phienThiId);
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        return buildLayNoiDungBaiThi(pOpt.get());
    }

    @Transactional
    public ApiResponse<Void> luuTraLoiAnDanh(String phienThiId, SinhVienLuuTraLoiDTO body) {
        Optional<PhienThi> pOpt = timPhienThiAnDanh(phienThiId);
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        if (p.getThoiGianNop() != null) {
            return ApiResponse.error("Bài đã nộp, không thể lưu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        DeThi deThi = p.getDeThi();
        Set<String> cauTrongDe = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(deThi).stream()
                .map(dc -> dc.getCauHoi().getId())
                .collect(Collectors.toSet());
        Map<String, String> map = body != null ? body.getTraLoi() : Map.of();
        for (Map.Entry<String, String> e : map.entrySet()) {
            String cauId = e.getKey();
            if (cauId == null || !cauTrongDe.contains(cauId)) {
                continue;
            }
            Optional<CauHoi> cauOpt = cauHoiRepository.findById(cauId);
            if (cauOpt.isEmpty()) {
                continue;
            }
            CauHoi cau = cauOpt.get();
            String val;
            if ("DUNG_SAI".equals(cau.getLoaiCauHoi())) {
                val = chuanHoaDungSai(e.getValue());
            } else {
                val = chuanHoaLuaChon(e.getValue());
            }
            if (val.isEmpty()) {
                continue;
            }
            Optional<CauTraLoi> ex = cauTraLoiRepository.findByPhienThiAndCauHoi(p, cau);
            CauTraLoi ctl;
            if (ex.isPresent()) {
                ctl = ex.get();
            } else {
                ctl = new CauTraLoi();
                ctl.setId(UUID.randomUUID().toString());
                ctl.setPhienThi(p);
                ctl.setCauHoi(cau);
            }
            ctl.setNoiDungTraLoi(val);
            ctl.setTrangThaiTraLoi(TTL_CHUA_TRA_LOI);
            ctl.setTuDongCham(false);
            ctl.setDiem(null);
            cauTraLoiRepository.save(ctl);
        }
        return ApiResponse.success("Đã lưu bài làm.", null);
    }

    @Transactional
    public ApiResponse<SinhVienKetQuaThiDTO> nopBaiAnDanh(String phienThiId) {
        Optional<PhienThi> pOpt = timPhienThiAnDanh(phienThiId);
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        if (p.getThoiGianNop() != null) {
            return ketQuaThiRepository.findByPhienThi(p)
                    .map(k -> ApiResponse.success("Đã nộp trước đó.", buildKetQuaDto(p, k)))
                    .orElseGet(() -> ApiResponse.error("Phiên đã nộp nhưng thiếu kết quả.", AuthService.ERR_HE_THONG));
        }
        return nopBaiCore(p);
    }

    @Transactional(readOnly = true)
    public ApiResponse<SinhVienKetQuaThiDTO> layKetQuaAnDanh(String phienThiId) {
        Optional<PhienThi> pOpt = timPhienThiAnDanh(phienThiId);
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        Optional<KetQuaThi> kOpt = ketQuaThiRepository.findByPhienThi(p);
        if (kOpt.isEmpty()) {
            return ApiResponse.error("Chưa có kết quả (chưa nộp bài).", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        return ApiResponse.success("OK", buildKetQuaDto(p, kOpt.get()));
    }

    @Transactional(readOnly = true)
    public ApiResponse<SinhVienLichSuChiTietDTO> layChiTietLichSuAnDanh(String phienThiId) {
        Optional<PhienThi> pOpt = timPhienThiAnDanh(phienThiId);
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        Optional<KetQuaThi> kOpt = ketQuaThiRepository.findByPhienThi(p);
        if (kOpt.isEmpty()) {
            return ApiResponse.error("Chưa có dữ liệu kết quả.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        KetQuaThi kq = kOpt.get();
        DeThi d = p.getDeThi();
        SinhVienLichSuChiTietDTO dto = new SinhVienLichSuChiTietDTO();
        dto.setPhienThiId(p.getId());
        dto.setTenDeThi(d.getTen());
        dto.setTenMonHoc(d.getMonHoc() != null ? d.getMonHoc().getTen() : "");
        if (kq.getThoiGianNop() != null) {
            dto.setThoiGianNop(kq.getThoiGianNop().format(ISO_DT));
        }
        dto.setTongDiem(kq.getTongDiem() != null ? kq.getTongDiem().stripTrailingZeros().toPlainString() : "0");
        dto.setDiemToiDa(layThangDiemToiDaHieuLuc(d).stripTrailingZeros().toPlainString());
        Map<String, CauTraLoi> traLoiTheoCau = cauTraLoiRepository.findByPhienThi(p).stream()
                .collect(Collectors.toMap(x -> x.getCauHoi().getId(), x -> x, (a, b) -> a));
        List<SinhVienCauXemLaiDTO> cacCau = new ArrayList<>();
        int stt = 0;
        int dung = 0;
        for (DeThiCauHoi link : deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(d)) {
            stt++;
            CauHoi c = link.getCauHoi();
            CauTraLoi ctl = traLoiTheoCau.get(c.getId());
            boolean isDung = ctl != null && TTL_DUNG.equals(ctl.getTrangThaiTraLoi());
            if (isDung) {
                dung++;
            }
            SinhVienCauXemLaiDTO cx = new SinhVienCauXemLaiDTO();
            cx.setStt(stt);
            cx.setNoiDung(c.getNoiDung());
            cx.setLoaiCauHoi(c.getLoaiCauHoi());
            cx.setLuaChonA(c.getLuaChonA());
            cx.setLuaChonB(c.getLuaChonB());
            cx.setLuaChonC(c.getLuaChonC());
            cx.setLuaChonD(c.getLuaChonD());
            cx.setDaChon(ctl != null && ctl.getNoiDungTraLoi() != null ? ctl.getNoiDungTraLoi() : null);
            cx.setDapAnDung(c.getDapAnDung());
            cx.setDung(isDung);
            cacCau.add(cx);
        }
        dto.setCacCau(cacCau);
        dto.setSoCauDung(dung);
        dto.setTongSoCau(cacCau.size());
        return ApiResponse.success("OK", dto);
    }

    private Optional<PhienThi> timPhienThiAnDanh(String phienThiId) {
        Optional<PhienThi> opt = phienThiRepository.findById(phienThiId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        PhienThi p = opt.get();
        if (p.getNguoiDung() != null) {
            return Optional.empty();
        }
        if (p.getHoTenAnDanh() == null || p.getHoTenAnDanh().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(p);
    }

    private static String chuanHoaHoTenAnDanh(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("\\s+", " ");
    }

    private ApiResponse<SinhVienBaiThiDTO> buildLayNoiDungBaiThi(PhienThi p) {
        if (p.getThoiGianNop() != null) {
            return ApiResponse.error("Bài thi đã được nộp.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        DeThi deThi = p.getDeThi();
        if (!hopLeThoiGianMoDong(deThi)) {
            return ApiResponse.error("Đề thi không trong khung giờ cho phép.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        Map<String, CauTraLoi> traLoiTheoCau = cauTraLoiRepository.findByPhienThi(p).stream()
                .collect(Collectors.toMap(x -> x.getCauHoi().getId(), x -> x, (a, b) -> a));
        SinhVienBaiThiDTO dto = new SinhVienBaiThiDTO();
        dto.setPhienThiId(p.getId());
        dto.setDeThiId(deThi.getId());
        dto.setTenDeThi(deThi.getTen());
        dto.setTenMonHoc(deThi.getMonHoc() != null ? deThi.getMonHoc().getTen() : "");
        dto.setMaHocPhan(deThi.getMonHoc() != null ? "MH-" + deThi.getMonHoc().getId().substring(0, Math.min(8, deThi.getMonHoc().getId().length())) : "—");
        dto.setThoiGianPhut(deThi.getThoiGianPhut() != null ? deThi.getThoiGianPhut() : 60);
        if (p.getThoiGianBatDau() != null) {
            dto.setThoiGianBatDau(p.getThoiGianBatDau().format(ISO_DT));
            int phut = dto.getThoiGianPhut();
            dto.setThoiGianHetHan(p.getThoiGianBatDau().plusMinutes(phut).format(ISO_DT));
        }
        List<SinhVienCauHoiThiDTO> cauList = new ArrayList<>();
        for (DeThiCauHoi link : deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(deThi)) {
            CauHoi c = link.getCauHoi();
            SinhVienCauHoiThiDTO cDto = new SinhVienCauHoiThiDTO();
            cDto.setId(c.getId());
            cDto.setThuTu(link.getThuTu() != null ? link.getThuTu() : 0);
            cDto.setNoiDung(c.getNoiDung());
            cDto.setLoaiCauHoi(c.getLoaiCauHoi());
            cDto.setLuaChonA(c.getLuaChonA());
            cDto.setLuaChonB(c.getLuaChonB());
            cDto.setLuaChonC(c.getLuaChonC());
            cDto.setLuaChonD(c.getLuaChonD());
            CauTraLoi ctl = traLoiTheoCau.get(c.getId());
            if (ctl != null && ctl.getNoiDungTraLoi() != null && !ctl.getNoiDungTraLoi().isBlank()) {
                if ("DUNG_SAI".equals(c.getLoaiCauHoi())) {
                    cDto.setDaChon(chuanHoaDungSai(ctl.getNoiDungTraLoi()));
                } else {
                    cDto.setDaChon(chuanHoaLuaChon(ctl.getNoiDungTraLoi()));
                }
            }
            cauList.add(cDto);
        }
        dto.setCauHoi(cauList);
        return ApiResponse.success("OK", dto);
    }

    private ApiResponse<SinhVienKetQuaThiDTO> nopBaiCore(PhienThi p) {
        DeThi deThi = p.getDeThi();
        List<DeThiCauHoi> links = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(deThi);
        int n = links.size();
        BigDecimal thang = layThangDiemToiDaHieuLuc(deThi);
        BigDecimal diemMoiCau = tinhDiemMoiCau(thang, n);
        if (diemMoiCau == null) {
            diemMoiCau = BigDecimal.ZERO;
        }
        BigDecimal tong = BigDecimal.ZERO;
        int soDung = 0;
        List<SinhVienOTrangThaiCauDTO> oTrangThai = new ArrayList<>();
        int stt = 0;
        for (DeThiCauHoi link : links) {
            stt++;
            CauHoi c = link.getCauHoi();
            Optional<CauTraLoi> ex = cauTraLoiRepository.findByPhienThiAndCauHoi(p, c);
            CauTraLoi ctl = ex.orElseGet(() -> {
                CauTraLoi x = new CauTraLoi();
                x.setId(UUID.randomUUID().toString());
                x.setPhienThi(p);
                x.setCauHoi(c);
                return x;
            });
            String traLoiSv = ctl.getNoiDungTraLoi() != null ? ctl.getNoiDungTraLoi() : "";
            boolean dung = chamTuDong(c, traLoiSv);
            BigDecimal diemCau = dung ? diemMoiCau : BigDecimal.ZERO;
            if (dung) {
                soDung++;
                tong = tong.add(diemCau);
            }
            if (!traLoiSv.isBlank()) {
                if ("DUNG_SAI".equals(c.getLoaiCauHoi())) {
                    ctl.setNoiDungTraLoi(chuanHoaDungSai(traLoiSv));
                } else {
                    ctl.setNoiDungTraLoi(chuanHoaLuaChon(traLoiSv));
                }
            }
            ctl.setTuDongCham(true);
            ctl.setDiem(diemCau);
            ctl.setTrangThaiTraLoi(dung ? TTL_DUNG : TTL_SAI);
            cauTraLoiRepository.save(ctl);
            SinhVienOTrangThaiCauDTO o = new SinhVienOTrangThaiCauDTO();
            o.setStt(stt);
            o.setDung(dung);
            oTrangThai.add(o);
        }
        LocalDateTime nopLuc = LocalDateTime.now();
        p.setThoiGianNop(nopLuc);
        p.setTrangThai(TT_DA_NOP);
        phienThiRepository.save(p);
        KetQuaThi kq = new KetQuaThi();
        kq.setId(UUID.randomUUID().toString());
        kq.setPhienThi(p);
        kq.setTongDiem(tong.setScale(2, RoundingMode.HALF_UP));
        kq.setThoiGianNop(nopLuc);
        kq.setTrangThaiCham("TU_DONG");
        ketQuaThiRepository.save(kq);
        SinhVienKetQuaThiDTO dto = buildKetQuaDto(p, kq);
        dto.setTrangThaiCacCau(oTrangThai);
        dto.setSoCauDung(soDung);
        dto.setTongSoCau(n);
        return ApiResponse.success("Nộp bài thành công.", dto);
    }

    @Transactional(readOnly = true)
    public ApiResponse<SinhVienBaiThiDTO> layNoiDungBaiThi(String sinhVienId, String phienThiId) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<PhienThi> pOpt = phienThiRepository.findByIdAndNguoiDung(phienThiId, svOpt.get());
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        return buildLayNoiDungBaiThi(p);
    }

    @Transactional
    public ApiResponse<Void> luuTraLoi(String sinhVienId, String phienThiId, SinhVienLuuTraLoiDTO body) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<PhienThi> pOpt = phienThiRepository.findByIdAndNguoiDung(phienThiId, svOpt.get());
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        if (p.getThoiGianNop() != null) {
            return ApiResponse.error("Bài đã nộp, không thể lưu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        DeThi deThi = p.getDeThi();

        Set<String> cauTrongDe = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(deThi).stream()
                .map(dc -> dc.getCauHoi().getId())
                .collect(Collectors.toSet());

        Map<String, String> map = body != null ? body.getTraLoi() : Map.of();
        for (Map.Entry<String, String> e : map.entrySet()) {
            String cauId = e.getKey();
            if (cauId == null || !cauTrongDe.contains(cauId)) {
                continue;
            }
            Optional<CauHoi> cauOpt = cauHoiRepository.findById(cauId);
            if (cauOpt.isEmpty()) {
                continue;
            }
            CauHoi cau = cauOpt.get();
            String val;
            if ("DUNG_SAI".equals(cau.getLoaiCauHoi())) {
                val = chuanHoaDungSai(e.getValue());
            } else {
                val = chuanHoaLuaChon(e.getValue());
            }
            if (val.isEmpty()) {
                continue;
            }
            Optional<CauTraLoi> ex = cauTraLoiRepository.findByPhienThiAndCauHoi(p, cau);
            CauTraLoi ctl;
            if (ex.isPresent()) {
                ctl = ex.get();
            } else {
                ctl = new CauTraLoi();
                ctl.setId(UUID.randomUUID().toString());
                ctl.setPhienThi(p);
                ctl.setCauHoi(cau);
            }
            ctl.setNoiDungTraLoi(val);
            ctl.setTrangThaiTraLoi(TTL_CHUA_TRA_LOI);
            ctl.setTuDongCham(false);
            ctl.setDiem(null);
            cauTraLoiRepository.save(ctl);
        }
        return ApiResponse.success("Đã lưu bài làm.", null);
    }

    @Transactional
    public ApiResponse<SinhVienKetQuaThiDTO> nopBai(String sinhVienId, String phienThiId) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<PhienThi> pOpt = phienThiRepository.findByIdAndNguoiDung(phienThiId, svOpt.get());
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        if (p.getThoiGianNop() != null) {
            return ketQuaThiRepository.findByPhienThi(p)
                    .map(k -> ApiResponse.success("Đã nộp trước đó.", buildKetQuaDto(p, k)))
                    .orElseGet(() -> ApiResponse.error("Phiên đã nộp nhưng thiếu kết quả.", AuthService.ERR_HE_THONG));
        }
        return nopBaiCore(p);
    }

    @Transactional(readOnly = true)
    public ApiResponse<SinhVienKetQuaThiDTO> layKetQua(String sinhVienId, String phienThiId) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<PhienThi> pOpt = phienThiRepository.findByIdAndNguoiDung(phienThiId, svOpt.get());
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        Optional<KetQuaThi> kOpt = ketQuaThiRepository.findByPhienThi(p);
        if (kOpt.isEmpty()) {
            return ApiResponse.error("Chưa có kết quả (chưa nộp bài).", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        return ApiResponse.success("OK", buildKetQuaDto(p, kOpt.get()));
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SinhVienLichSuThiItemDTO>> layLichSu(String sinhVienId) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        List<KetQuaThi> list = ketQuaThiRepository.findByNguoiDung(svOpt.get());
        List<SinhVienLichSuThiItemDTO> out = new ArrayList<>();
        for (KetQuaThi kq : list) {
            PhienThi p = kq.getPhienThi();
            DeThi d = p.getDeThi();
            SinhVienLichSuThiItemDTO it = new SinhVienLichSuThiItemDTO();
            it.setPhienThiId(p.getId());
            it.setTenDeThi(d.getTen());
            it.setTenMonHoc(d.getMonHoc() != null ? d.getMonHoc().getTen() : "");
            if (kq.getThoiGianNop() != null) {
                it.setThoiGianNop(kq.getThoiGianNop().format(ISO_DT));
            }
            it.setTongDiem(kq.getTongDiem() != null ? kq.getTongDiem().stripTrailingZeros().toPlainString() : "0");
            BigDecimal thang = layThangDiemToiDaHieuLuc(d);
            it.setDiemToiDa(thang.stripTrailingZeros().toPlainString());
            long n = deThiCauHoiRepository.countByDeThi(d);
            it.setTongSoCau((int) n);
            int dung = (int) cauTraLoiRepository.findByPhienThi(p).stream()
                    .filter(c -> TTL_DUNG.equals(c.getTrangThaiTraLoi()))
                    .count();
            it.setSoCauDung(dung);
            out.add(it);
        }
        return ApiResponse.success("OK", out);
    }

    @Transactional(readOnly = true)
    public ApiResponse<SinhVienLichSuChiTietDTO> layChiTietLichSu(String sinhVienId, String phienThiId) {
        Optional<NguoiDung> svOpt = nguoiDungRepository.findById(sinhVienId);
        if (svOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy sinh viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<PhienThi> pOpt = phienThiRepository.findByIdAndNguoiDung(phienThiId, svOpt.get());
        if (pOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhienThi p = pOpt.get();
        Optional<KetQuaThi> kOpt = ketQuaThiRepository.findByPhienThi(p);
        if (kOpt.isEmpty()) {
            return ApiResponse.error("Chưa có dữ liệu kết quả.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        KetQuaThi kq = kOpt.get();
        DeThi d = p.getDeThi();

        SinhVienLichSuChiTietDTO dto = new SinhVienLichSuChiTietDTO();
        dto.setPhienThiId(p.getId());
        dto.setTenDeThi(d.getTen());
        dto.setTenMonHoc(d.getMonHoc() != null ? d.getMonHoc().getTen() : "");
        if (kq.getThoiGianNop() != null) {
            dto.setThoiGianNop(kq.getThoiGianNop().format(ISO_DT));
        }
        dto.setTongDiem(kq.getTongDiem() != null ? kq.getTongDiem().stripTrailingZeros().toPlainString() : "0");
        dto.setDiemToiDa(layThangDiemToiDaHieuLuc(d).stripTrailingZeros().toPlainString());

        Map<String, CauTraLoi> traLoiTheoCau = cauTraLoiRepository.findByPhienThi(p).stream()
                .collect(Collectors.toMap(x -> x.getCauHoi().getId(), x -> x, (a, b) -> a));

        List<SinhVienCauXemLaiDTO> cacCau = new ArrayList<>();
        int stt = 0;
        int dung = 0;
        for (DeThiCauHoi link : deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(d)) {
            stt++;
            CauHoi c = link.getCauHoi();
            CauTraLoi ctl = traLoiTheoCau.get(c.getId());
            boolean isDung = ctl != null && TTL_DUNG.equals(ctl.getTrangThaiTraLoi());
            if (isDung) {
                dung++;
            }
            SinhVienCauXemLaiDTO cx = new SinhVienCauXemLaiDTO();
            cx.setStt(stt);
            cx.setNoiDung(c.getNoiDung());
            cx.setLoaiCauHoi(c.getLoaiCauHoi());
            cx.setLuaChonA(c.getLuaChonA());
            cx.setLuaChonB(c.getLuaChonB());
            cx.setLuaChonC(c.getLuaChonC());
            cx.setLuaChonD(c.getLuaChonD());
            cx.setDaChon(ctl != null && ctl.getNoiDungTraLoi() != null ? ctl.getNoiDungTraLoi() : null);
            cx.setDapAnDung(c.getDapAnDung());
            cx.setDung(isDung);
            cacCau.add(cx);
        }
        dto.setCacCau(cacCau);
        dto.setSoCauDung(dung);
        dto.setTongSoCau(cacCau.size());
        return ApiResponse.success("OK", dto);
    }

    private SinhVienKetQuaThiDTO buildKetQuaDto(PhienThi p, KetQuaThi kq) {
        DeThi d = p.getDeThi();
        SinhVienKetQuaThiDTO dto = new SinhVienKetQuaThiDTO();
        dto.setPhienThiId(p.getId());
        dto.setTenDeThi(d.getTen());
        if (kq.getThoiGianNop() != null) {
            dto.setNgayThi(kq.getThoiGianNop().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        dto.setMaHocPhan(d.getMonHoc() != null ? "MH-" + d.getMonHoc().getId().substring(0, Math.min(8, d.getMonHoc().getId().length())) : "—");
        dto.setTenGiaoVien(formatHoTen(d.getNguoiDung()));
        dto.setDiemDat(kq.getTongDiem() != null ? kq.getTongDiem().stripTrailingZeros().toPlainString() : "0");
        BigDecimal thang = layThangDiemToiDaHieuLuc(d);
        dto.setDiemToiDa(thang.stripTrailingZeros().toPlainString());
        dto.setThoiGianGioiHanPhut(d.getThoiGianPhut() != null ? d.getThoiGianPhut() : 60);
        if (p.getThoiGianBatDau() != null && kq.getThoiGianNop() != null) {
            Duration dur = Duration.between(p.getThoiGianBatDau(), kq.getThoiGianNop());
            long sec = Math.max(0, dur.getSeconds());
            dto.setThoiGianLamBai(String.format("%02d:%02d", sec / 60, sec % 60));
        } else {
            dto.setThoiGianLamBai("00:00");
        }

        List<DeThiCauHoi> links = deThiCauHoiRepository.findByDeThiOrderByThuTuAsc(d);
        dto.setTongSoCau(links.size());
        int sd = (int) cauTraLoiRepository.findByPhienThi(p).stream()
                .filter(c -> TTL_DUNG.equals(c.getTrangThaiTraLoi()))
                .count();
        dto.setSoCauDung(sd);

        List<SinhVienOTrangThaiCauDTO> oList = new ArrayList<>();
        int i = 0;
        for (DeThiCauHoi link : links) {
            i++;
            CauHoi c = link.getCauHoi();
            Optional<CauTraLoi> ctl = cauTraLoiRepository.findByPhienThiAndCauHoi(p, c);
            boolean du = ctl.map(t -> TTL_DUNG.equals(t.getTrangThaiTraLoi())).orElse(false);
            SinhVienOTrangThaiCauDTO o = new SinhVienOTrangThaiCauDTO();
            o.setStt(i);
            o.setDung(du);
            oList.add(o);
        }
        dto.setTrangThaiCacCau(oList);
        return dto;
    }

    private static boolean hopLeThoiGianMoDong(DeThi d) {
        LocalDateTime now = LocalDateTime.now();
        if (d.getThoiGianMo() != null && now.isBefore(d.getThoiGianMo())) {
            return false;
        }
        if (d.getThoiGianDong() != null && now.isAfter(d.getThoiGianDong())) {
            return false;
        }
        return true;
    }

    private static boolean chamTuDong(CauHoi c, String traLoiSv) {
        String loai = c.getLoaiCauHoi();
        String dapAn = c.getDapAnDung();
        if (dapAn == null) {
            return false;
        }
        if ("TRAC_NGHIEM".equals(loai) || loai == null) {
            return chuanHoaLuaChon(traLoiSv).equals(chuanHoaLuaChon(dapAn));
        }
        if ("DUNG_SAI".equals(loai)) {
            return chuanHoaDungSai(traLoiSv).equals(chuanHoaDungSai(dapAn));
        }
        return false;
    }

    private static String chuanHoaDungSai(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (t.startsWith("D") && t.contains("UNG")) {
            return "DUNG";
        }
        if (t.startsWith("S") || t.equals("SAI")) {
            return "SAI";
        }
        return t;
    }

    private static String chuanHoaLuaChon(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (t.isEmpty()) {
            return "";
        }
        char c = t.charAt(0);
        if (c >= 'A' && c <= 'D') {
            return String.valueOf(c);
        }
        return t;
    }

    private static BigDecimal layThangDiemToiDaHieuLuc(DeThi deThi) {
        BigDecimal t = deThi.getThangDiemToiDa();
        if (t == null || t.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.TEN;
        }
        return t;
    }

    private static BigDecimal tinhDiemMoiCau(BigDecimal thangDiemToiDa, int soCau) {
        if (soCau <= 0 || thangDiemToiDa == null) {
            return null;
        }
        return thangDiemToiDa.divide(BigDecimal.valueOf(soCau), 6, RoundingMode.HALF_UP);
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
