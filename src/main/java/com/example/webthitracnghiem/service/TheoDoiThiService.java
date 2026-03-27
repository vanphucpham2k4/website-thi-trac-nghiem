package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.TheoDoiDeThiOptionDTO;
import com.example.webthitracnghiem.dto.TheoDoiSinhVienThiDTO;
import com.example.webthitracnghiem.dto.TheoDoiSinhVienThiItemDTO;
import com.example.webthitracnghiem.entity.DeThi;
import com.example.webthitracnghiem.entity.NguoiDung;
import com.example.webthitracnghiem.entity.PhienThi;
import com.example.webthitracnghiem.repository.DeThiRepository;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.PhienThiRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TheoDoiThiService {

    public static final String TT_CHUA_VAO_THI = "CHUA_VAO_THI";
    public static final String TT_DANG_THI = "DANG_THI";
    public static final String TT_DA_NOP_BAI = "DA_NOP_BAI";
    public static final String TT_DA_VAO_CHUA_NOP = "DA_VAO_CHUA_NOP";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final NguoiDungRepository nguoiDungRepository;
    private final DeThiRepository deThiRepository;
    private final PhienThiRepository phienThiRepository;

    public TheoDoiThiService(
            NguoiDungRepository nguoiDungRepository,
            DeThiRepository deThiRepository,
            PhienThiRepository phienThiRepository
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.deThiRepository = deThiRepository;
        this.phienThiRepository = phienThiRepository;
    }

    public TheoDoiSinhVienThiDTO layDanhSachTheoDoi(String giaoVienId, String deThiId, String nhomTrangThaiFilter, String keyword) {
        Optional<NguoiDung> giaoVienOpt = nguoiDungRepository.findById(giaoVienId);
        if (giaoVienOpt.isEmpty()) {
            return null;
        }

        Optional<DeThi> deThiOpt = deThiRepository.findByIdAndNguoiDung(deThiId, giaoVienOpt.get());
        if (deThiOpt.isEmpty()) {
            return null;
        }

        DeThi deThi = deThiOpt.get();
        List<NguoiDung> sinhVienList = nguoiDungRepository.findByVaiTro(AuthService.ROLE_SINH_VIEN);
        List<PhienThi> phienThiList = phienThiRepository.findByDeThiId(deThiId);

        Map<String, PhienThi> phienThiMoiNhatTheoSinhVien = new HashMap<>();
        for (PhienThi phienThi : phienThiList) {
            if (phienThi.getNguoiDung() == null || phienThi.getNguoiDung().getId() == null) {
                continue;
            }
            String sinhVienId = phienThi.getNguoiDung().getId();
            PhienThi hienTai = phienThiMoiNhatTheoSinhVien.get(sinhVienId);
            if (hienTai == null || soSanhMoiNhat(phienThi, hienTai) > 0) {
                phienThiMoiNhatTheoSinhVien.put(sinhVienId, phienThi);
            }
        }

        TheoDoiSinhVienThiDTO dto = new TheoDoiSinhVienThiDTO();
        dto.setDeThiId(deThi.getId());
        dto.setTenDeThi(deThi.getTen());

        List<TheoDoiSinhVienThiItemDTO> danhSach = new ArrayList<>();
        for (NguoiDung sinhVien : sinhVienList) {
            PhienThi phienThi = phienThiMoiNhatTheoSinhVien.get(sinhVien.getId());
            String nhomTrangThai = xacDinhNhomTrangThai(phienThi, deThi);
            if (!hopLeTheoFilter(nhomTrangThaiFilter, nhomTrangThai)) {
                continue;
            }
            if (!hopLeTheoTuKhoa(sinhVien, keyword)) {
                continue;
            }
            danhSach.add(taoItem(sinhVien, phienThi, deThi, nhomTrangThai));
        }

        danhSach.sort(Comparator.comparing(TheoDoiSinhVienThiItemDTO::getHoTen, Comparator.nullsLast(String::compareToIgnoreCase)));
        dto.setDanhSach(danhSach);
        dto.setTongSo(danhSach.size());
        dto.setSoChuaVaoThi(demTheoTrangThai(danhSach, TT_CHUA_VAO_THI));
        dto.setSoDangThi(demTheoTrangThai(danhSach, TT_DANG_THI));
        dto.setSoDaNopBai(demTheoTrangThai(danhSach, TT_DA_NOP_BAI));
        dto.setSoDaVaoChuaNop(demTheoTrangThai(danhSach, TT_DA_VAO_CHUA_NOP));
        return dto;
    }

    public List<TheoDoiDeThiOptionDTO> layDanhSachDeThiTheoGiaoVien(String giaoVienId) {
        Optional<NguoiDung> giaoVienOpt = nguoiDungRepository.findById(giaoVienId);
        if (giaoVienOpt.isEmpty()) {
            return new ArrayList<>();
        }
        return deThiRepository.findByNguoiDung(giaoVienOpt.get()).stream()
                .map(deThi -> {
                    TheoDoiDeThiOptionDTO dto = new TheoDoiDeThiOptionDTO();
                    dto.setId(deThi.getId());
                    dto.setMaDeThi(deThi.getMaDeThi());
                    dto.setTenDeThi(deThi.getTen());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private static int soSanhMoiNhat(PhienThi a, PhienThi b) {
        LocalDateTime da = a.getThoiGianBatDau() != null ? a.getThoiGianBatDau() : a.getThoiGianNop();
        LocalDateTime db = b.getThoiGianBatDau() != null ? b.getThoiGianBatDau() : b.getThoiGianNop();
        if (da == null && db == null) {
            return 0;
        }
        if (da == null) {
            return -1;
        }
        if (db == null) {
            return 1;
        }
        return da.compareTo(db);
    }

    private static long demTheoTrangThai(List<TheoDoiSinhVienThiItemDTO> danhSach, String trangThai) {
        return danhSach.stream().filter(item -> trangThai.equals(item.getNhomTrangThai())).count();
    }

    private static boolean hopLeTheoFilter(String filter, String trangThai) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return filter.trim().equalsIgnoreCase(trangThai);
    }

    private static boolean hopLeTheoTuKhoa(NguoiDung sv, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String key = keyword.trim().toLowerCase();
        return chua(sv.getHoTen(), key) || chua(sv.getEmail(), key) || chua(sv.getMaNguoiDung(), key);
    }

    private static boolean chua(String text, String key) {
        return text != null && text.toLowerCase().contains(key);
    }

    private static TheoDoiSinhVienThiItemDTO taoItem(NguoiDung sv, PhienThi phienThi, DeThi deThi, String nhomTrangThai) {
        TheoDoiSinhVienThiItemDTO item = new TheoDoiSinhVienThiItemDTO();
        item.setSinhVienId(sv.getId());
        item.setMaNguoiDung(sv.getMaNguoiDung());
        item.setHoTen(sv.getHoTen());
        item.setEmail(sv.getEmail());
        item.setDeThiId(deThi.getId());
        item.setTenDeThi(deThi.getTen());
        item.setNhomTrangThai(nhomTrangThai);
        if (phienThi != null) {
            item.setPhienThiId(phienThi.getId());
            item.setThoiGianBatDau(formatTime(phienThi.getThoiGianBatDau()));
            item.setThoiGianNop(formatTime(phienThi.getThoiGianNop()));
        }
        return item;
    }

    private static String formatTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }

    private static String xacDinhNhomTrangThai(PhienThi phienThi, DeThi deThi) {
        if (phienThi == null) {
            return TT_CHUA_VAO_THI;
        }
        if (phienThi.getThoiGianNop() != null) {
            return TT_DA_NOP_BAI;
        }
        if (phienThi.getThoiGianBatDau() == null) {
            return TT_CHUA_VAO_THI;
        }

        int thoiGianPhut = deThi.getThoiGianPhut() != null ? deThi.getThoiGianPhut() : 0;
        LocalDateTime hanNop = phienThi.getThoiGianBatDau().plusMinutes(thoiGianPhut);
        if (LocalDateTime.now().isAfter(hanNop)) {
            return TT_DA_VAO_CHUA_NOP;
        }
        return TT_DANG_THI;
    }
}
