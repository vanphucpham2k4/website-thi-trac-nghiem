package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.DoiThuongRequestDTO;
import com.example.webthitracnghiem.dto.DoiThuongTongQuanDTO;
import com.example.webthitracnghiem.dto.PhanThuongCardDTO;
import com.example.webthitracnghiem.dto.YeuCauDoiThuongDTO;
import com.example.webthitracnghiem.model.LoaiPhanThuong;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.PhanThuong;
import com.example.webthitracnghiem.model.TrangThaiYeuCauDoiThuong;
import com.example.webthitracnghiem.model.YeuCauDoiThuong;
import com.example.webthitracnghiem.repository.KetQuaThiRepository;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.PhanThuongRepository;
import com.example.webthitracnghiem.repository.YeuCauDoiThuongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class DoiThuongService {

    public static final String CARD_CO_THE_DOI = "CO_THE_DOI";
    public static final String CARD_KHONG_DU_DIEM = "KHONG_DU_DIEM";
    public static final String CARD_HET_HANG = "HET_HANG";

    /**
     * Các trạng thái yêu cầu đổi được tính vào “tổng lượt đổi thành công” (đã ghi nhận, chưa hủy).
     * Gồm cả {@code CHO_DUYET} vì sau khi sinh viên xác nhận đổi hệ thống chưa có bước duyệt admin.
     */
    private static final List<TrangThaiYeuCauDoiThuong> TRANG_THAI_DEM_LUOT_DOI_GHI_NHAN = List.of(
            TrangThaiYeuCauDoiThuong.CHO_DUYET,
            TrangThaiYeuCauDoiThuong.DA_DUYET,
            TrangThaiYeuCauDoiThuong.DA_NHAN_QUA);

    private final NguoiDungRepository nguoiDungRepository;
    private final KetQuaThiRepository ketQuaThiRepository;
    private final PhanThuongRepository phanThuongRepository;
    private final YeuCauDoiThuongRepository yeuCauDoiThuongRepository;

    public DoiThuongService(
            NguoiDungRepository nguoiDungRepository,
            KetQuaThiRepository ketQuaThiRepository,
            PhanThuongRepository phanThuongRepository,
            YeuCauDoiThuongRepository yeuCauDoiThuongRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.ketQuaThiRepository = ketQuaThiRepository;
        this.phanThuongRepository = phanThuongRepository;
        this.yeuCauDoiThuongRepository = yeuCauDoiThuongRepository;
    }

    public ApiResponse<DoiThuongTongQuanDTO> layTongQuan(String userId) {
        Optional<NguoiDung> opt = nguoiDungRepository.findById(userId);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        NguoiDung nd = opt.get();
        int tichLuy = nd.getDiemThuongTichLuy();
        long rawDaDung = yeuCauDoiThuongRepository.sumDiemDaDungChuaHuy(nd, TrangThaiYeuCauDoiThuong.DA_HUY);
        int daDung = (int) Math.min(Integer.MAX_VALUE, rawDaDung);
        int soDu = Math.max(0, tichLuy - daDung);
        long thanhCong = yeuCauDoiThuongRepository.countByNguoiDungAndTrangThaiIn(nd, TRANG_THAI_DEM_LUOT_DOI_GHI_NHAN);
        if (nd.getTongLuotDoiThanhCong() != thanhCong) {
            nd.setTongLuotDoiThanhCong(thanhCong);
            nguoiDungRepository.save(nd);
        }

        DoiThuongTongQuanDTO dto = new DoiThuongTongQuanDTO();
        dto.setDiemHienTai(soDu);
        dto.setTongLuotDoiThanhCong(thanhCong);
        dto.setDiemTichLuyTuBaiThi(tichLuy);
        dto.setDiemDaSuDung(daDung);
        return ApiResponse.success("OK", dto);
    }

    /**
     * Ghi lên {@link NguoiDung}: điểm thưởng = tổng điểm các kết quả thi; lượt đổi = số yêu cầu đã ghi nhận (chờ duyệt / đã duyệt / đã nhận quà, trừ đã hủy).
     * <p><b>Chỉ gọi khi cần đồng bộ từ nguồn</b> (vd. sau khi nộp bài), không gọi mỗi lần đọc trang — nếu không mọi chỉnh tay
     * trong DB sẽ bị ghi đè bởi tổng điểm từ {@code ket_qua_thi}.
     */
    @Transactional
    public Optional<NguoiDung> dongBoDiemThuongVaLuotDoiLenDb(String nguoiDungId) {
        if (nguoiDungId == null || nguoiDungId.isBlank()) {
            return Optional.empty();
        }
        Optional<NguoiDung> opt = nguoiDungRepository.findById(nguoiDungId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        NguoiDung nd = opt.get();
        int diem = lamTronDiemThi(ketQuaThiRepository.tongDiemTichLuy(nd));
        long luot = yeuCauDoiThuongRepository.countByNguoiDungAndTrangThaiIn(nd, TRANG_THAI_DEM_LUOT_DOI_GHI_NHAN);
        nd.setDiemThuongTichLuy(diem);
        nd.setTongLuotDoiThanhCong(luot);
        return Optional.of(nguoiDungRepository.save(nd));
    }

    public ApiResponse<List<PhanThuongCardDTO>> layPhanThuongCards(
            String userId,
            String q,
            String loaiRaw,
            String mucDiem,
            String locTrangThai) {
        Optional<NguoiDung> opt = nguoiDungRepository.findById(userId);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        int soDu = tinhSoDu(opt.get());
        LoaiPhanThuong loai = parseLoai(loaiRaw);
        String qn = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        String loc = locTrangThai == null || locTrangThai.isBlank() ? "TAT_CA" : locTrangThai.trim().toUpperCase(Locale.ROOT);

        List<PhanThuongCardDTO> out = new ArrayList<>();
        for (PhanThuong p : phanThuongRepository.findByHienThiTrueOrderByThuTuAscDiemDoiAsc()) {
            if (loai != null && p.getLoai() != loai) {
                continue;
            }
            if (!qn.isEmpty()) {
                String ten = p.getTen() != null ? p.getTen().toLowerCase(Locale.ROOT) : "";
                String mt = p.getMoTaNgan() != null ? p.getMoTaNgan().toLowerCase(Locale.ROOT) : "";
                if (!ten.contains(qn) && !mt.contains(qn)) {
                    continue;
                }
            }
            if (!khopMucDiem(p.getDiemDoi(), mucDiem)) {
                continue;
            }
            PhanThuongCardDTO c = toCard(p, soDu);
            if (!"TAT_CA".equals(loc) && !loc.equals(c.getTrangThaiCard())) {
                continue;
            }
            out.add(c);
        }
        return ApiResponse.success("OK", out);
    }

    public ApiResponse<List<YeuCauDoiThuongDTO>> layLichSu(
            String userId,
            String q,
            String trangThaiRaw,
            String khoang) {
        Optional<NguoiDung> opt = nguoiDungRepository.findById(userId);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        String qn = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        TrangThaiYeuCauDoiThuong ttLoc = parseTrangThaiLoc(trangThaiRaw);
        LocalDateTime tu = null;
        LocalDateTime den = null;
        String k = khoang == null || khoang.isBlank() ? "TAT_CA" : khoang.trim().toUpperCase(Locale.ROOT);
        switch (k) {
            case "HOM_NAY" -> {
                tu = LocalDate.now().atStartOfDay();
                den = LocalDate.now().atTime(LocalTime.MAX);
            }
            case "7_NGAY" -> tu = LocalDateTime.now().minusDays(7);
            case "30_NGAY" -> tu = LocalDateTime.now().minusDays(30);
            default -> { }
        }

        List<YeuCauDoiThuongDTO> out = new ArrayList<>();
        for (YeuCauDoiThuong y : yeuCauDoiThuongRepository.findByNguoiDungIdWithPhanThuong(userId)) {
            if (ttLoc != null && y.getTrangThai() != ttLoc) {
                continue;
            }
            if (tu != null && y.getThoiGian().isBefore(tu)) {
                continue;
            }
            if (den != null && y.getThoiGian().isAfter(den)) {
                continue;
            }
            if (!qn.isEmpty()) {
                String ten = y.getPhanThuong().getTen() != null
                        ? y.getPhanThuong().getTen().toLowerCase(Locale.ROOT) : "";
                if (!ten.contains(qn)) {
                    continue;
                }
            }
            out.add(toDto(y));
        }
        return ApiResponse.success("OK", out);
    }

    public ApiResponse<YeuCauDoiThuongDTO> layChiTiet(String userId, String yeuCauId) {
        return yeuCauDoiThuongRepository.findByIdAndNguoiDungIdWithPhanThuong(yeuCauId, userId)
                .map(y -> ApiResponse.success("OK", toDto(y)))
                .orElseGet(() -> ApiResponse.error("Không tìm thấy yêu cầu.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI));
    }

    @Transactional
    public ApiResponse<YeuCauDoiThuongDTO> doiThuong(String userId, DoiThuongRequestDTO body) {
        String phanThuongId = body != null ? body.getPhanThuongId() : null;
        if (phanThuongId == null || phanThuongId.isBlank()) {
            return ApiResponse.error("Chưa chọn phần thưởng.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        NguoiDung nd = nguoiDungRepository.findById(userId).orElse(null);
        if (nd == null) {
            return ApiResponse.error("Không tìm thấy người dùng.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        PhanThuong pt = phanThuongRepository.findById(phanThuongId).orElse(null);
        if (pt == null || !pt.isHienThi()) {
            return ApiResponse.error("Phần thưởng không tồn tại.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        int soDu = tinhSoDu(nd);
        if (pt.getSoLuongConLai() < 1) {
            return ApiResponse.error("Phần thưởng hiện đã hết hàng.");
        }
        if (soDu < pt.getDiemDoi()) {
            return ApiResponse.error("Bạn không đủ điểm để đổi phần thưởng này.");
        }
        final String maDoiMoi;
        try {
            maDoiMoi = sinhMaDoiThuong6KyTuDuNhat();
        } catch (IllegalStateException e) {
            return ApiResponse.error("Không tạo được mã đổi thưởng. Vui lòng thử lại.");
        }
        pt.setSoLuongConLai(pt.getSoLuongConLai() - 1);
        phanThuongRepository.save(pt);

        YeuCauDoiThuong y = new YeuCauDoiThuong();
        y.setId(UUID.randomUUID().toString());
        y.setMaDoi(maDoiMoi);
        y.setNguoiDung(nd);
        y.setPhanThuong(pt);
        y.setDiemDaDung(pt.getDiemDoi());
        y.setThoiGian(LocalDateTime.now());
        y.setTrangThai(TrangThaiYeuCauDoiThuong.CHO_DUYET);
        yeuCauDoiThuongRepository.save(y);
        return ApiResponse.success("Đổi thưởng thành công.", toDto(y));
    }

    @Transactional
    public ApiResponse<YeuCauDoiThuongDTO> huyYeuCau(String userId, String yeuCauId) {
        if (yeuCauId == null || yeuCauId.isBlank()) {
            return ApiResponse.error("Thiếu mã yêu cầu.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        YeuCauDoiThuong y = yeuCauDoiThuongRepository.findByIdAndNguoiDungIdWithPhanThuong(yeuCauId, userId).orElse(null);
        if (y == null) {
            return ApiResponse.error("Không tìm thấy yêu cầu.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        if (y.getTrangThai() != TrangThaiYeuCauDoiThuong.CHO_DUYET) {
            return ApiResponse.error("Chỉ có thể hủy yêu cầu đang chờ duyệt.");
        }
        y.setTrangThai(TrangThaiYeuCauDoiThuong.DA_HUY);
        PhanThuong pt = y.getPhanThuong();
        pt.setSoLuongConLai(pt.getSoLuongConLai() + 1);
        phanThuongRepository.save(pt);
        yeuCauDoiThuongRepository.save(y);
        return ApiResponse.success("Đã hủy yêu cầu đổi thưởng.", toDto(y));
    }

    private int tinhSoDu(NguoiDung nd) {
        int tichLuy = nd.getDiemThuongTichLuy();
        long rawDaDung = yeuCauDoiThuongRepository.sumDiemDaDungChuaHuy(nd, TrangThaiYeuCauDoiThuong.DA_HUY);
        int daDung = (int) Math.min(Integer.MAX_VALUE, rawDaDung);
        return Math.max(0, tichLuy - daDung);
    }

    private static int lamTronDiemThi(BigDecimal sum) {
        if (sum == null) {
            return 0;
        }
        BigDecimal down = sum.setScale(0, RoundingMode.DOWN);
        BigDecimal cap = BigDecimal.valueOf(Integer.MAX_VALUE);
        return down.compareTo(cap) > 0 ? Integer.MAX_VALUE : down.intValue();
    }

    private static final String MA_DOI_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom MA_DOI_RANDOM = new SecureRandom();

    /**
     * Mã đổi thưởng 6 ký tự (A–Z và 0–9), duy nhất trong hệ thống.
     */
    private String sinhMaDoiThuong6KyTuDuNhat() {
        for (int attempt = 0; attempt < 48; attempt++) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(MA_DOI_ALPHABET.charAt(MA_DOI_RANDOM.nextInt(MA_DOI_ALPHABET.length())));
            }
            String m = sb.toString();
            if (!yeuCauDoiThuongRepository.existsByMaDoi(m)) {
                return m;
            }
        }
        throw new IllegalStateException("Không sinh được mã đổi thưởng duy nhất.");
    }

    private static LoaiPhanThuong parseLoai(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String x = s.trim().toUpperCase(Locale.ROOT);
        if ("TAT_CA".equals(x)) {
            return null;
        }
        try {
            return LoaiPhanThuong.valueOf(x);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static TrangThaiYeuCauDoiThuong parseTrangThaiLoc(String s) {
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

    private static boolean khopMucDiem(int diem, String muc) {
        if (muc == null || muc.isBlank() || "TAT_CA".equalsIgnoreCase(muc.trim())) {
            return true;
        }
        return switch (muc.trim().toUpperCase(Locale.ROOT)) {
            case "DUOI_200" -> diem < 200;
            case "TU_200_DEN_500" -> diem >= 200 && diem < 500;
            case "TU_500_DEN_1000" -> diem >= 500 && diem < 1000;
            case "TREN_1000" -> diem >= 1000;
            default -> true;
        };
    }

    private static PhanThuongCardDTO toCard(PhanThuong p, int soDu) {
        PhanThuongCardDTO c = new PhanThuongCardDTO();
        c.setId(p.getId());
        c.setTen(p.getTen());
        c.setMoTaNgan(p.getMoTaNgan());
        c.setLoai(p.getLoai().name());
        c.setDiemDoi(p.getDiemDoi());
        c.setSoLuongConLai(p.getSoLuongConLai());
        c.setIconClass(p.getIconClass() != null && !p.getIconClass().isBlank() ? p.getIconClass() : "fas fa-gift");
        c.setAnhUrl(p.getAnhUrl());
        String card;
        if (p.getSoLuongConLai() < 1) {
            card = CARD_HET_HANG;
        } else if (soDu < p.getDiemDoi()) {
            card = CARD_KHONG_DU_DIEM;
        } else {
            card = CARD_CO_THE_DOI;
        }
        c.setTrangThaiCard(card);
        return c;
    }

    private static YeuCauDoiThuongDTO toDto(YeuCauDoiThuong y) {
        YeuCauDoiThuongDTO d = new YeuCauDoiThuongDTO();
        d.setId(y.getId());
        d.setMaDoi(y.getMaDoi());
        d.setPhanThuongId(y.getPhanThuong().getId());
        d.setTenPhanThuong(y.getPhanThuong().getTen());
        d.setMoTaNgan(y.getPhanThuong().getMoTaNgan());
        d.setDiemDaDung(y.getDiemDaDung());
        d.setThoiGian(y.getThoiGian().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        d.setTrangThai(y.getTrangThai().name());
        d.setGhiChu(y.getGhiChu());
        return d;
    }
}
