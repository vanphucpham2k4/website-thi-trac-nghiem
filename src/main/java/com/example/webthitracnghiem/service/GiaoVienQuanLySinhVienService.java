package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.ApiResponse;
import com.example.webthitracnghiem.dto.GiaoVienSinhVienListItemDTO;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.PhienThiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Danh sách sinh viên cho giao diện Quản lý sinh viên (giáo viên).
 */
@Service
public class GiaoVienQuanLySinhVienService {

    private final NguoiDungRepository nguoiDungRepository;
    private final PhienThiRepository phienThiRepository;

    public GiaoVienQuanLySinhVienService(
            NguoiDungRepository nguoiDungRepository,
            PhienThiRepository phienThiRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.phienThiRepository = phienThiRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<GiaoVienSinhVienListItemDTO>> layDanhSach(String giaoVienId, String keyword) {
        if (giaoVienId == null || giaoVienId.isBlank()) {
            return ApiResponse.error("Không xác định được giáo viên.", AuthService.ERR_HE_THONG);
        }

        List<NguoiDung> sinhVien = nguoiDungRepository.findByVaiTro(AuthService.ROLE_SINH_VIEN);
        Map<String, Long> luotTheoSv = new HashMap<>();
        for (Object[] row : phienThiRepository.demSoPhienThiTheoSinhVienCuaGiaoVien(giaoVienId)) {
            if (row[0] != null && row[1] != null) {
                luotTheoSv.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        String kw = keyword != null ? keyword.trim().toLowerCase(Locale.ROOT) : "";

        List<GiaoVienSinhVienListItemDTO> list = sinhVien.stream()
                .filter(sv -> khopTuKhoa(sv, kw))
                .map(sv -> chuyenDto(sv, luotTheoSv.getOrDefault(sv.getId(), 0L)))
                .sorted(Comparator.comparing(GiaoVienSinhVienListItemDTO::getHoTen,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        return ApiResponse.success("OK", list);
    }

    private static boolean khopTuKhoa(NguoiDung sv, String kw) {
        if (kw.isEmpty()) {
            return true;
        }
        String ma = sv.getMaNguoiDung() != null ? sv.getMaNguoiDung().toLowerCase(Locale.ROOT) : "";
        String ht = sv.getHoTen() != null ? sv.getHoTen().toLowerCase(Locale.ROOT) : "";
        String mail = sv.getEmail() != null ? sv.getEmail().toLowerCase(Locale.ROOT) : "";
        String sdt = sv.getSoDienThoai() != null ? sv.getSoDienThoai().toLowerCase(Locale.ROOT) : "";
        return ma.contains(kw) || ht.contains(kw) || mail.contains(kw) || sdt.contains(kw);
    }

    private static GiaoVienSinhVienListItemDTO chuyenDto(NguoiDung sv, long soLuot) {
        GiaoVienSinhVienListItemDTO dto = new GiaoVienSinhVienListItemDTO();
        dto.setId(sv.getId());
        dto.setMaNguoiDung(sv.getMaNguoiDung());
        dto.setHoTen(sv.getHoTen());
        dto.setEmail(sv.getEmail());
        dto.setSoDienThoai(sv.getSoDienThoai());
        dto.setSoLuotThiVoiGiaoVien(soLuot);
        return dto;
    }
}
