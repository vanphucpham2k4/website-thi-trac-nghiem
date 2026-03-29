package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.GiaoVienKetQuaDeThiItemDTO;
import com.example.webthitracnghiem.dto.GiaoVienKetQuaLopItemDTO;
import com.example.webthitracnghiem.dto.GiaoVienKetQuaSinhVienItemDTO;
import com.example.webthitracnghiem.model.*;
import com.example.webthitracnghiem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service xử lý nghiệp vụ Xem Kết Quả Chi Tiết cho Giáo viên.
 * Luồng 1 (theo lớp): Danh sách lớp → Đề thi trong lớp → Kết quả (lớp + ẩn danh).
 * Luồng 2 (tất cả đề): Danh sách đề thi → Kết quả toàn bộ.
 */
@Service
public class GiaoVienKetQuaService {

    private static final DateTimeFormatter FMT_DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NguoiDungRepository nguoiDungRepository;
    private final LopHocRepository lopHocRepository;
    private final LopHocSinhVienRepository lopHocSinhVienRepository;
    private final DeThiLopHocRepository deThiLopHocRepository;
    private final DeThiRepository deThiRepository;
    private final PhienThiRepository phienThiRepository;
    private final KetQuaThiRepository ketQuaThiRepository;

    public GiaoVienKetQuaService(NguoiDungRepository nguoiDungRepository,
                                  LopHocRepository lopHocRepository,
                                  LopHocSinhVienRepository lopHocSinhVienRepository,
                                  DeThiLopHocRepository deThiLopHocRepository,
                                  DeThiRepository deThiRepository,
                                  PhienThiRepository phienThiRepository,
                                  KetQuaThiRepository ketQuaThiRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.lopHocRepository = lopHocRepository;
        this.lopHocSinhVienRepository = lopHocSinhVienRepository;
        this.deThiLopHocRepository = deThiLopHocRepository;
        this.deThiRepository = deThiRepository;
        this.phienThiRepository = phienThiRepository;
        this.ketQuaThiRepository = ketQuaThiRepository;
    }

    // ========== Luồng 1 — Bước 1: Danh sách lớp ==========

    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienKetQuaLopItemDTO>> layDanhSachLop(String giaoVienId) {
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        NguoiDung gv = gvOpt.get();
        List<LopHoc> dsLop = lopHocRepository.findByGiaoVienOrderByThoiGianTaoDesc(gv);

        List<GiaoVienKetQuaLopItemDTO> out = new ArrayList<>();
        for (LopHoc lop : dsLop) {
            GiaoVienKetQuaLopItemDTO dto = new GiaoVienKetQuaLopItemDTO();
            dto.setId(lop.getId());
            dto.setTenLop(lop.getTen());
            dto.setSoSinhVien(lopHocSinhVienRepository.countByLopHoc(lop));
            dto.setSoDeThiXuatBan(deThiLopHocRepository.countByLopHoc(lop));
            out.add(dto);
        }
        return ApiResponse.success("OK", out);
    }

    // ========== Luồng 1 — Bước 2: Danh sách đề thi trong lớp ==========

    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienKetQuaDeThiItemDTO>> layDanhSachDeThiCuaLop(String giaoVienId, String lopId) {
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findByIdAndGiaoVien(lopId, gvOpt.get());
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp học hoặc bạn không có quyền.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        LopHoc lop = lopOpt.get();
        List<DeThiLopHoc> dsXuatBan = deThiLopHocRepository.findByLopHocFetchDeThi(lop);

        List<GiaoVienKetQuaDeThiItemDTO> out = new ArrayList<>();
        for (DeThiLopHoc dtlh : dsXuatBan) {
            DeThi dt = dtlh.getDeThi();
            if (dt.getDeletedAt() != null) continue;

            GiaoVienKetQuaDeThiItemDTO dto = new GiaoVienKetQuaDeThiItemDTO();
            dto.setDeThiId(dt.getId());
            dto.setMaDeThi(dt.getMaDeThi());
            dto.setTenDeThi(dt.getTen());
            dto.setTenMonHoc(dt.getMonHoc() != null ? dt.getMonHoc().getTen() : "");
            dto.setThoiGianPhut(dt.getThoiGianPhut());
            dto.setSoLuotThi(phienThiRepository.countByDeThiAndLopHoc(dt, lop));
            out.add(dto);
        }
        return ApiResponse.success("OK", out);
    }

    // ========== Luồng 1 — Bước 3: Kết quả sinh viên (lớp + ẩn danh) ==========

    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienKetQuaSinhVienItemDTO>> layKetQuaSinhVien(
            String giaoVienId, String lopId, String deThiId) {
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<LopHoc> lopOpt = lopHocRepository.findByIdAndGiaoVien(lopId, gvOpt.get());
        if (lopOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy lớp học hoặc bạn không có quyền.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        String tenLop = lopOpt.get().getTen();

        // Lấy kết quả: (thuộc lớp) UNION (ẩn danh, lopHoc = null)
        List<KetQuaThi> dsKetQua = ketQuaThiRepository.findByDeThiIdAndLopHocIdOrAnonymous(deThiId, lopId);

        return ApiResponse.success("OK", buildKetQuaRows(dsKetQua, tenLop));
    }

    // ========== Luồng 2 — Danh sách tất cả đề thi của giáo viên ==========

    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienKetQuaDeThiItemDTO>> layDanhSachDeThiCuaGiaoVien(String giaoVienId) {
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        List<DeThi> dsDeThi = deThiRepository.findByNguoiDung(gvOpt.get());

        List<GiaoVienKetQuaDeThiItemDTO> out = new ArrayList<>();
        for (DeThi dt : dsDeThi) {
            if (dt.getDeletedAt() != null) continue;

            GiaoVienKetQuaDeThiItemDTO dto = new GiaoVienKetQuaDeThiItemDTO();
            dto.setDeThiId(dt.getId());
            dto.setMaDeThi(dt.getMaDeThi());
            dto.setTenDeThi(dt.getTen());
            dto.setTenMonHoc(dt.getMonHoc() != null ? dt.getMonHoc().getTen() : "");
            dto.setThoiGianPhut(dt.getThoiGianPhut());

            // Đếm tổng lượt thi = lấy ALL kết quả
            List<KetQuaThi> allKq = ketQuaThiRepository.findAllByDeThiId(dt.getId());
            dto.setSoLuotThi(allKq.size());
            // Đếm ẩn danh
            long anDanh = allKq.stream()
                    .filter(kq -> kq.getPhienThi().getLopHoc() == null)
                    .count();
            dto.setSoLuotThiAnDanh(anDanh);

            out.add(dto);
        }
        return ApiResponse.success("OK", out);
    }

    // ========== Luồng 2 — Kết quả toàn bộ theo đề thi ==========

    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienKetQuaSinhVienItemDTO>> layKetQuaTheoDeThiId(
            String giaoVienId, String deThiId) {
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        // Kiểm tra đề thi thuộc giáo viên
        Optional<DeThi> dtOpt = deThiRepository.findById(deThiId);
        if (dtOpt.isEmpty() || !dtOpt.get().getNguoiDung().getId().equals(giaoVienId)) {
            return ApiResponse.error("Không tìm thấy đề thi hoặc bạn không có quyền.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        List<KetQuaThi> dsKetQua = ketQuaThiRepository.findAllByDeThiId(deThiId);

        return ApiResponse.success("OK", buildKetQuaRows(dsKetQua, null));
    }

    // ========== Cập nhật ghi chú ==========

    @Transactional
    public ApiResponse<Void> capNhatGhiChu(String giaoVienId, String ketQuaThiId, String ghiChu) {
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<KetQuaThi> kqOpt = ketQuaThiRepository.findById(ketQuaThiId);
        if (kqOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy kết quả thi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        KetQuaThi kq = kqOpt.get();

        DeThi dt = kq.getPhienThi().getDeThi();
        if (!dt.getNguoiDung().getId().equals(giaoVienId)) {
            return ApiResponse.error("Bạn không có quyền sửa kết quả này.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        kq.setGhiChu(ghiChu);
        ketQuaThiRepository.save(kq);
        return ApiResponse.success("Cập nhật ghi chú thành công.", null);
    }

    // ========== Cập nhật điểm ==========

    @Transactional
    public ApiResponse<Void> capNhatDiem(String giaoVienId, String ketQuaThiId, BigDecimal diem) {
        Optional<NguoiDung> gvOpt = nguoiDungRepository.findById(giaoVienId);
        if (gvOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản giáo viên.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        Optional<KetQuaThi> kqOpt = ketQuaThiRepository.findById(ketQuaThiId);
        if (kqOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy kết quả thi.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        KetQuaThi kq = kqOpt.get();

        DeThi dt = kq.getPhienThi().getDeThi();
        if (!dt.getNguoiDung().getId().equals(giaoVienId)) {
            return ApiResponse.error("Bạn không có quyền sửa kết quả này.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        if (diem != null && diem.compareTo(BigDecimal.ZERO) < 0) {
            return ApiResponse.error("Điểm không được âm.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }

        kq.setTongDiem(diem);
        ketQuaThiRepository.save(kq);
        return ApiResponse.success("Cập nhật điểm thành công.", null);
    }

    // ========== Helper: Xây dựng danh sách DTO từ KetQuaThi ==========

    /**
     * @param dsKetQua  danh sách kết quả thi
     * @param tenLopMacDinh  nếu không null → dùng cho row có lopHoc khớp; nếu null → lấy tên lớp từ phienThi
     */
    private List<GiaoVienKetQuaSinhVienItemDTO> buildKetQuaRows(
            List<KetQuaThi> dsKetQua, String tenLopMacDinh) {

        List<GiaoVienKetQuaSinhVienItemDTO> out = new ArrayList<>();
        int stt = 0;
        for (KetQuaThi kq : dsKetQua) {
            stt++;
            PhienThi pt = kq.getPhienThi();
            NguoiDung sv = pt.getNguoiDung();
            DeThi dt = pt.getDeThi();

            GiaoVienKetQuaSinhVienItemDTO dto = new GiaoVienKetQuaSinhVienItemDTO();
            dto.setStt(stt);
            dto.setKetQuaThiId(kq.getId());

            if (sv != null) {
                dto.setMssv(sv.getMaNguoiDung() != null ? sv.getMaNguoiDung() : "");
                dto.setHo(sv.getHo() != null ? sv.getHo() : "");
                dto.setTen(sv.getTen() != null ? sv.getTen() : "");
            } else {
                dto.setMssv("—");
                dto.setHo(pt.getHoTenAnDanh() != null ? pt.getHoTenAnDanh() : "Ẩn danh");
                dto.setTen("");
            }

            dto.setDuongDanTruyCap(dt.getDuongDanTruyCap() != null ? dt.getDuongDanTruyCap() : "");
            dto.setMaTruyCapDaDung(pt.getMaTruyCapDaDung() != null ? pt.getMaTruyCapDaDung() : "");
            dto.setDiem(kq.getTongDiem() != null ? kq.getTongDiem().toPlainString() : "—");
            dto.setThoiGianNop(kq.getThoiGianNop() != null ? kq.getThoiGianNop().format(FMT_DT) : "—");
            dto.setGhiChu(kq.getGhiChu() != null ? kq.getGhiChu() : "");

            // Nguồn
            LopHoc lop = pt.getLopHoc();
            if (lop == null) {
                dto.setNguon("Link công khai");
            } else if (tenLopMacDinh != null) {
                dto.setNguon(tenLopMacDinh);
            } else {
                dto.setNguon(lop.getTen() != null ? lop.getTen() : "Lớp");
            }

            out.add(dto);
        }
        return out;
    }
}
