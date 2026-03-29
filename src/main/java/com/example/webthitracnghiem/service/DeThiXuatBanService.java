package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.GiaoVienLopDaXuatBanDTO;
import com.example.webthitracnghiem.dto.GiaoVienLopHocSelectDTO;
import com.example.webthitracnghiem.dto.XuatBanDeThiChoLopDTO;
import com.example.webthitracnghiem.model.DeThi;
import com.example.webthitracnghiem.model.DeThiLopHoc;
import com.example.webthitracnghiem.model.LopHoc;
import com.example.webthitracnghiem.model.LopHocSinhVien;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.repository.DeThiCauHoiRepository;
import com.example.webthitracnghiem.repository.DeThiLopHocRepository;
import com.example.webthitracnghiem.repository.DeThiRepository;
import com.example.webthitracnghiem.repository.LopHocRepository;
import com.example.webthitracnghiem.repository.LopHocSinhVienRepository;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.PhienThiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Xuất bản đề thi công khai vào lớp do giáo viên chủ trì.
 */
@Service
public class DeThiXuatBanService {

    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DeThiRepository deThiRepository;
    private final LopHocRepository lopHocRepository;
    private final DeThiLopHocRepository deThiLopHocRepository;
    private final DeThiCauHoiRepository deThiCauHoiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final LopHocSinhVienRepository lopHocSinhVienRepository;
    private final PhienThiRepository phienThiRepository;

    public DeThiXuatBanService(
            DeThiRepository deThiRepository,
            LopHocRepository lopHocRepository,
            DeThiLopHocRepository deThiLopHocRepository,
            DeThiCauHoiRepository deThiCauHoiRepository,
            NguoiDungRepository nguoiDungRepository,
            LopHocSinhVienRepository lopHocSinhVienRepository,
            PhienThiRepository phienThiRepository) {
        this.deThiRepository = deThiRepository;
        this.lopHocRepository = lopHocRepository;
        this.deThiLopHocRepository = deThiLopHocRepository;
        this.deThiCauHoiRepository = deThiCauHoiRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.lopHocSinhVienRepository = lopHocSinhVienRepository;
        this.phienThiRepository = phienThiRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienLopHocSelectDTO>> layDanhSachLopChoDropdown(String giaoVienId) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        List<LopHoc> lops = lopHocRepository.findByGiaoVienOrderByThoiGianTaoDesc(gvOpt.get());
        List<GiaoVienLopHocSelectDTO> out = new ArrayList<>();
        for (LopHoc l : lops) {
            GiaoVienLopHocSelectDTO d = new GiaoVienLopHocSelectDTO();
            d.setId(l.getId());
            d.setTenLop(l.getTen());
            out.add(d);
        }
        return ApiResponse.success("OK", out);
    }

    @Transactional
    public ApiResponse<Void> xuatBanChoLop(String giaoVienId, String deThiId, XuatBanDeThiChoLopDTO body) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        String lopId = body != null && body.getLopHocId() != null ? body.getLopHocId().trim() : "";
        if (lopId.isEmpty()) {
            return ApiResponse.error("Vui lòng chọn lớp học.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        Optional<DeThi> deOpt = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (deOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi hoặc đề đã bị xóa.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = deOpt.get();
        if (!deThi.getNguoiDung().getId().equals(giaoVienId)) {
            return ApiResponse.error("Bạn không có quyền xuất bản đề thi này.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }
        if (!"CONG_KHAI".equals(deThi.getTrangThai())) {
            return ApiResponse.error("Chỉ đề ở trạng thái \"Công khai\" mới được xuất bản vào lớp.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        long soCau = deThiCauHoiRepository.countByDeThi(deThi);
        if (soCau <= 0) {
            return ApiResponse.error("Đề thi chưa có câu hỏi, không thể xuất bản.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findByIdAndGiaoVien(lopId, gvOpt.get());
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp hoặc lớp không thuộc bạn.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();

        LocalDateTime now = LocalDateTime.now();
        Optional<DeThiLopHoc> lkOpt = deThiLopHocRepository.findByDeThiAndLopHoc(deThi, lop);
        if (lkOpt.isPresent()) {
            DeThiLopHoc lk = lkOpt.get();
            lk.setThoiGianXuatBan(now);
            deThiLopHocRepository.save(lk);
            return ApiResponse.success("Đã cập nhật thời điểm xuất bản đề vào lớp.", null);
        }

        DeThiLopHoc moi = new DeThiLopHoc();
        moi.setId(UUID.randomUUID().toString());
        moi.setDeThi(deThi);
        moi.setLopHoc(lop);
        moi.setThoiGianXuatBan(now);
        deThiLopHocRepository.save(moi);
        return ApiResponse.success("Xuất bản đề thi vào lớp thành công.", null);
    }

    /**
     * Lấy danh sách lớp mà đề đã được xuất bản (kèm thời gian, số SV, số SV đã mở bài).
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienLopDaXuatBanDTO>> layDanhSachLopDaXuatBan(String giaoVienId, String deThiId) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<DeThi> deOpt = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (deOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = deOpt.get();
        if (!deThi.getNguoiDung().getId().equals(giaoVienId)) {
            return ApiResponse.error("Bạn không có quyền với đề thi này.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }
        List<DeThiLopHoc> links = deThiLopHocRepository.findByDeThi(deThi);
        List<GiaoVienLopDaXuatBanDTO> out = new ArrayList<>();
        for (DeThiLopHoc lk : links) {
            LopHoc lop = lk.getLopHoc();
            GiaoVienLopDaXuatBanDTO d = new GiaoVienLopDaXuatBanDTO();
            d.setId(lop.getId());
            d.setTenLop(lop.getTen());
            if (lk.getThoiGianXuatBan() != null) {
                d.setThoiGianXuatBan(lk.getThoiGianXuatBan().format(ISO_DT));
            }
            long sv = lopHocSinhVienRepository.countByLopHoc(lop);
            d.setSoSinhVien(sv);
            out.add(d);
        }
        return ApiResponse.success("OK", out);
    }

    /**
     * Thu hồi đề đã xuất bản khỏi một lớp.
     * Cảnh báo: SV đang làm dở vẫn có thể tiếp tục làm bài.
     */
    @Transactional
    public ApiResponse<Void> thuHoiChoLop(String giaoVienId, String deThiId, XuatBanDeThiChoLopDTO body) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }
        String lopId = body != null && body.getLopHocId() != null ? body.getLopHocId().trim() : "";
        if (lopId.isEmpty()) {
            return ApiResponse.error("Vui lòng chọn lớp cần thu hồi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<DeThi> deOpt = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (deOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = deOpt.get();
        if (!deThi.getNguoiDung().getId().equals(giaoVienId)) {
            return ApiResponse.error("Bạn không có quyền với đề thi này.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findByIdAndGiaoVien(lopId, gvOpt.get());
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Lớp không thuộc quyền quản lý của bạn.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();
        Optional<DeThiLopHoc> lkOpt = deThiLopHocRepository.findByDeThiAndLopHoc(deThi, lop);
        if (lkOpt.isEmpty()) {
            return ApiResponse.error("Đề thi chưa được xuất bản cho lớp này.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        long svDangLam = phienThiRepository.countByDeThiAndLopHoc(deThi, lop);
        deThiLopHocRepository.delete(lkOpt.get());
        if (svDangLam > 0) {
            return ApiResponse.success(
                    "Đã thu hồi đề khỏi lớp. " + svDangLam + " sinh viên đang làm dở bài thi (vẫn tiếp tục làm được).",
                    null);
        }
        return ApiResponse.success("Đã thu hồi đề khỏi lớp thành công.", null);
    }
}
