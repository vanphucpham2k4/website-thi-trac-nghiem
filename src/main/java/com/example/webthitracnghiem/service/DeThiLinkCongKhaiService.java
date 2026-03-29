package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.DeThiLinkThamGiaResponseDTO;
import com.example.webthitracnghiem.dto.PublicDeThiLinkThongTinDTO;
import com.example.webthitracnghiem.dto.TaoLinkThamGiaDTO;
import com.example.webthitracnghiem.model.DeThi;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.repository.DeThiCauHoiRepository;
import com.example.webthitracnghiem.repository.DeThiRepository;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Link công khai tham gia đề thi (khác xuất bản theo lớp): dùng {@link DeThi#getMaTruyCap()}.
 */
@Service
public class DeThiLinkCongKhaiService {

    public static final String TIEN_TO_DUONG_DAN = "/thi-mo/";

    private final DeThiRepository deThiRepository;
    private final DeThiCauHoiRepository deThiCauHoiRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public DeThiLinkCongKhaiService(
            DeThiRepository deThiRepository,
            DeThiCauHoiRepository deThiCauHoiRepository,
            NguoiDungRepository nguoiDungRepository) {
        this.deThiRepository = deThiRepository;
        this.deThiCauHoiRepository = deThiCauHoiRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Transactional
    public ApiResponse<DeThiLinkThamGiaResponseDTO> taoHoacCapNhatLink(String giaoVienId, String deThiId, TaoLinkThamGiaDTO body) {
        boolean taoMoi = body != null && body.isTaoMoi();
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
        if (!"CONG_KHAI".equals(deThi.getTrangThai())) {
            return ApiResponse.error("Chỉ đề công khai mới có thể tạo link tham gia.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (deThiCauHoiRepository.countByDeThi(deThi) <= 0) {
            return ApiResponse.error("Đề thi chưa có câu hỏi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        if (taoMoi || deThi.getMaTruyCap() == null || deThi.getMaTruyCap().isBlank()) {
            String ma = sinhMaTruyCapDuyNhat();
            deThi.setMaTruyCap(ma);
            deThi.setDuongDanTruyCap(TIEN_TO_DUONG_DAN + ma);
            deThiRepository.save(deThi);
        }

        DeThiLinkThamGiaResponseDTO dto = new DeThiLinkThamGiaResponseDTO();
        dto.setMaTruyCap(deThi.getMaTruyCap());
        dto.setDuongDanTuongDoi(TIEN_TO_DUONG_DAN + deThi.getMaTruyCap());
        return ApiResponse.success("OK", dto);
    }

    @Transactional
    public ApiResponse<Void> huyLinkThuHoi(String giaoVienId, String deThiId) {
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
        deThi.setMaTruyCap(null);
        deThi.setDuongDanTruyCap(null);
        deThiRepository.save(deThi);
        return ApiResponse.success("Đã thu hồi link tham gia.", null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<DeThiLinkThamGiaResponseDTO> layLinkHienTai(String giaoVienId, String deThiId) {
        Optional<DeThi> deOpt = deThiRepository.findByIdAndNotDeleted(deThiId);
        if (deOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        DeThi deThi = deOpt.get();
        if (!deThi.getNguoiDung().getId().equals(giaoVienId)) {
            return ApiResponse.error("Bạn không có quyền với đề thi này.", AuthService.ERR_VAI_TRO_KHONG_HOP_LE);
        }
        if (deThi.getMaTruyCap() == null || deThi.getMaTruyCap().isBlank()) {
            return ApiResponse.success("Chưa có link.", null);
        }
        DeThiLinkThamGiaResponseDTO dto = new DeThiLinkThamGiaResponseDTO();
        dto.setMaTruyCap(deThi.getMaTruyCap());
        dto.setDuongDanTuongDoi(TIEN_TO_DUONG_DAN + deThi.getMaTruyCap());
        return ApiResponse.success("OK", dto);
    }

    @Transactional(readOnly = true)
    public ApiResponse<PublicDeThiLinkThongTinDTO> layThongTinCongKhaiTheoMa(String maTruyCap) {
        if (maTruyCap == null || maTruyCap.isBlank()) {
            return ApiResponse.error("Mã tham gia không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        Optional<DeThi> deOpt = deThiRepository.findByMaTruyCapAndDeletedAtIsNull(maTruyCap.trim());
        if (deOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy đề thi hoặc link đã bị thu hồi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        DeThi deThi = deOpt.get();
        if (!"CONG_KHAI".equals(deThi.getTrangThai())) {
            return ApiResponse.error("Đề thi không khả dụng.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        long n = deThiCauHoiRepository.countByDeThi(deThi);
        if (n <= 0) {
            return ApiResponse.error("Đề thi chưa có câu hỏi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (!hopLeThoiGianMoDong(deThi)) {
            return ApiResponse.error("Đề thi hiện không trong khung giờ cho phép làm bài.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        PublicDeThiLinkThongTinDTO dto = new PublicDeThiLinkThongTinDTO();
        dto.setTenDeThi(deThi.getTen());
        dto.setTenMonHoc(deThi.getMonHoc() != null ? deThi.getMonHoc().getTen() : "");
        dto.setThoiGianPhut(deThi.getThoiGianPhut() != null ? deThi.getThoiGianPhut() : 60);
        dto.setSoCauHoi(n);
        return ApiResponse.success("OK", dto);
    }

    private static boolean hopLeThoiGianMoDong(DeThi d) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (d.getThoiGianMo() != null && now.isBefore(d.getThoiGianMo())) {
            return false;
        }
        if (d.getThoiGianDong() != null && now.isAfter(d.getThoiGianDong())) {
            return false;
        }
        return true;
    }

    private String sinhMaTruyCapDuyNhat() {
        for (int i = 0; i < 20; i++) {
            String ma = UUID.randomUUID().toString().replace("-", "");
            if (deThiRepository.findByMaTruyCapAndDeletedAtIsNull(ma).isEmpty()) {
                return ma;
            }
        }
        return UUID.randomUUID().toString().replace("-", "") + System.nanoTime();
    }
}
