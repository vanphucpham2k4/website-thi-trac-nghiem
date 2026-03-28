package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.AdminLuuMonHocDTO;
import com.example.webthitracnghiem.dto.AdminMonHocItemDTO;
import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.model.ChuDe;
import com.example.webthitracnghiem.model.MonHoc;
import com.example.webthitracnghiem.repository.CauHoiRepository;
import com.example.webthitracnghiem.repository.ChuDeRepository;
import com.example.webthitracnghiem.repository.DeThiRepository;
import com.example.webthitracnghiem.repository.MonHocRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD môn học dành cho Admin.
 */
@Service
public class AdminMonHocService {

    private static final int TEN_MAX = 255;

    private static final int CHU_DE_TEN_MAX = 255;

    private final MonHocRepository monHocRepository;
    private final ChuDeRepository chuDeRepository;
    private final DeThiRepository deThiRepository;
    private final CauHoiRepository cauHoiRepository;

    public AdminMonHocService(
            MonHocRepository monHocRepository,
            ChuDeRepository chuDeRepository,
            DeThiRepository deThiRepository,
            CauHoiRepository cauHoiRepository) {
        this.monHocRepository = monHocRepository;
        this.chuDeRepository = chuDeRepository;
        this.deThiRepository = deThiRepository;
        this.cauHoiRepository = cauHoiRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AdminMonHocItemDTO>> danhSach() {
        List<AdminMonHocItemDTO> list = monHocRepository.findAll(Sort.by(Sort.Direction.ASC, "ten")).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success("OK", list);
    }

    @Transactional(readOnly = true)
    public ApiResponse<AdminMonHocItemDTO> chiTiet(String id) {
        return monHocRepository.findById(id)
                .map(m -> ApiResponse.success("OK", toDto(m)))
                .orElseGet(() -> ApiResponse.error("Không tìm thấy môn học.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI));
    }

    @Transactional
    public ApiResponse<AdminMonHocItemDTO> taoMoi(AdminLuuMonHocDTO dto) {
        ApiResponse<Void> valid = validateDto(dto, true);
        if (!valid.isSuccess()) {
            return ApiResponse.error(valid.getMessage(), valid.getErrorCode());
        }
        String ten = dto.getTen().trim();
        if (tenTrungTen(ten, null)) {
            return ApiResponse.error("Tên môn học đã tồn tại.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        MonHoc mh = new MonHoc();
        mh.setId(UUID.randomUUID().toString());
        mh.setTen(ten);
        mh.setMoTa(normalizeMoTa(dto.getMoTa()));
        monHocRepository.save(mh);
        ApiResponse<Void> sync = dongBoChuDeTheoDong(mh, dto.getTenChuDeTheoDong());
        if (!sync.isSuccess()) {
            monHocRepository.delete(mh);
            return ApiResponse.error(sync.getMessage(), sync.getErrorCode());
        }
        return ApiResponse.success("Đã thêm môn học.", toDto(mh));
    }

    @Transactional
    public ApiResponse<AdminMonHocItemDTO> capNhat(String id, AdminLuuMonHocDTO dto) {
        ApiResponse<Void> valid = validateDto(dto, true);
        if (!valid.isSuccess()) {
            return ApiResponse.error(valid.getMessage(), valid.getErrorCode());
        }
        Optional<MonHoc> opt = monHocRepository.findById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy môn học.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        String ten = dto.getTen().trim();
        if (tenTrungTen(ten, id)) {
            return ApiResponse.error("Tên môn học đã tồn tại.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        MonHoc mh = opt.get();
        mh.setTen(ten);
        mh.setMoTa(normalizeMoTa(dto.getMoTa()));
        monHocRepository.save(mh);
        ApiResponse<Void> sync = dongBoChuDeTheoDong(mh, dto.getTenChuDeTheoDong());
        if (!sync.isSuccess()) {
            return ApiResponse.error(sync.getMessage(), sync.getErrorCode());
        }
        return ApiResponse.success("Đã cập nhật môn học.", toDto(mh));
    }

    /**
     * Đồng bộ danh sách chủ đề với nội dung nhiều dòng: thứ tự dòng khớp thứ tự bản ghi {@link ChuDe} theo id tăng dần.
     */
    private ApiResponse<Void> dongBoChuDeTheoDong(MonHoc mh, String raw) {
        List<String> wanted = parseChuDeLines(raw);
        Set<String> seen = new HashSet<>();
        for (String w : wanted) {
            if (!seen.add(w.toLowerCase(Locale.ROOT))) {
                return ApiResponse.error("Trùng tên chủ đề trong danh sách.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }

        List<ChuDe> existing = chuDeRepository.findByMonHocOrderByIdAsc(mh);
        int oldN = existing.size();
        int newN = wanted.size();

        for (int i = 0; i < Math.min(oldN, newN); i++) {
            String newTen = wanted.get(i);
            ChuDe c = existing.get(i);
            if (newTen.equals(c.getTen())) {
                continue;
            }
            if (coChuDeTrungTenKhacId(mh, newTen, c.getId())) {
                return ApiResponse.error("Tên chủ đề đã tồn tại trong môn: " + newTen, AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }
        for (int i = newN; i < oldN; i++) {
            ChuDe c = existing.get(i);
            long cnt = cauHoiRepository.countByChuDe(c);
            if (cnt > 0) {
                String tn = Optional.ofNullable(c.getTen()).orElse("");
                return ApiResponse.error(
                        "Không bỏ được chủ đề \"" + tn + "\" — đang có " + cnt + " câu hỏi. Giữ dòng này hoặc chuyển câu hỏi sang chủ đề khác.",
                        AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }
        for (int i = oldN; i < newN; i++) {
            if (coChuDeTrungTenKhacId(mh, wanted.get(i), null)) {
                return ApiResponse.error("Tên chủ đề đã tồn tại trong môn: " + wanted.get(i), AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
            }
        }

        for (int i = 0; i < Math.min(oldN, newN); i++) {
            String newTen = wanted.get(i);
            ChuDe c = existing.get(i);
            if (!newTen.equals(c.getTen())) {
                c.setTen(newTen);
                chuDeRepository.save(c);
            }
        }
        for (int i = newN; i < oldN; i++) {
            chuDeRepository.delete(existing.get(i));
        }
        for (int i = oldN; i < newN; i++) {
            ChuDe c = new ChuDe();
            c.setId(UUID.randomUUID().toString());
            c.setTen(wanted.get(i));
            c.setMonHoc(mh);
            chuDeRepository.save(c);
        }
        return ApiResponse.success("OK");
    }

    private List<String> parseChuDeLines(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>();
        for (String line : raw.split("\\R")) {
            String t = line.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (t.length() > CHU_DE_TEN_MAX) {
                t = t.substring(0, CHU_DE_TEN_MAX);
            }
            out.add(t);
        }
        return out;
    }

    private boolean coChuDeTrungTenKhacId(MonHoc mh, String ten, String excludeId) {
        String t = ten == null ? "" : ten.trim();
        return chuDeRepository.findByMonHoc(mh).stream()
                .anyMatch(c -> (excludeId == null || !excludeId.equals(c.getId()))
                        && t.equalsIgnoreCase(trimNullSafe(c.getTen())));
    }

    private static String trimNullSafe(String s) {
        return s == null ? "" : s.trim();
    }

    @Transactional
    public ApiResponse<Void> xoa(String id) {
        Optional<MonHoc> opt = monHocRepository.findById(id);
        if (opt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy môn học.", AuthService.ERR_TAI_KHOAN_KHONG_TON_TAI);
        }
        MonHoc mh = opt.get();
        long soChuDe = chuDeRepository.countByMonHoc(mh);
        long soDeThi = deThiRepository.countByMonHoc(mh);
        if (soChuDe > 0 || soDeThi > 0) {
            return ApiResponse.error(
                    "Không thể xóa: môn học đang có " + soChuDe + " chủ đề và " + soDeThi + " đề thi.",
                    AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        monHocRepository.delete(mh);
        return ApiResponse.success("Đã xóa môn học.");
    }

    private ApiResponse<Void> validateDto(AdminLuuMonHocDTO dto, boolean requireTen) {
        if (dto == null) {
            return ApiResponse.error("Dữ liệu không hợp lệ.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        String ten = dto.getTen() == null ? "" : dto.getTen().trim();
        if (requireTen && ten.isEmpty()) {
            return ApiResponse.error("Vui lòng nhập tên môn học.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        if (ten.length() > TEN_MAX) {
            return ApiResponse.error("Tên môn học tối đa " + TEN_MAX + " ký tự.", AuthService.ERR_DU_LIEU_KHONG_HOP_LE);
        }
        return ApiResponse.success("OK");
    }

    private String normalizeMoTa(String moTa) {
        if (moTa == null) {
            return null;
        }
        String t = moTa.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean tenTrungTen(String tenMoi, String boQuaId) {
        return monHocRepository.findAll().stream()
                .anyMatch(m -> (boQuaId == null || !boQuaId.equals(m.getId()))
                        && tenMoi.equalsIgnoreCase(m.getTen() != null ? m.getTen().trim() : ""));
    }

    private AdminMonHocItemDTO toDto(MonHoc m) {
        List<String> tenChuDe = chuDeRepository.findByMonHocOrderByIdAsc(m).stream()
                .map(ChuDe::getTen)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toList());
        AdminMonHocItemDTO dto = new AdminMonHocItemDTO();
        dto.setId(m.getId());
        dto.setTen(m.getTen());
        dto.setMoTa(m.getMoTa());
        dto.setDanhSachTenChuDe(tenChuDe);
        return dto;
    }
}
