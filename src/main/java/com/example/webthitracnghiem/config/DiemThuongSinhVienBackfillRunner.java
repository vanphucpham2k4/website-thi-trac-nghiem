package com.example.webthitracnghiem.config;

import com.example.webthitracnghiem.repository.NguoiDungVaiTroRepository;
import com.example.webthitracnghiem.service.AuthService;
import com.example.webthitracnghiem.service.DoiThuongService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tùy chọn: đồng bộ toàn bộ sinh viên từ {@code ket_qua_thi} / yêu cầu đổi thưởng khi khởi động.
 * Mặc định tắt — bật sẽ ghi đè {@code diem_thuong_tich_luy} đã chỉnh tay trong DB.
 */
@Component
@Order(45)
@ConditionalOnProperty(name = "doi-thuong.dong-bo-toan-bo-sinh-vien-khi-khoi-dong", havingValue = "true")
public class DiemThuongSinhVienBackfillRunner implements ApplicationRunner {

    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final DoiThuongService doiThuongService;

    public DiemThuongSinhVienBackfillRunner(
            NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
            DoiThuongService doiThuongService) {
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.doiThuongService = doiThuongService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> ids = nguoiDungVaiTroRepository.findDistinctNguoiDungIdsByVaiTroTen(AuthService.ROLE_SINH_VIEN);
        for (String id : ids) {
            doiThuongService.dongBoDiemThuongVaLuotDoiLenDb(id);
        }
    }
}
