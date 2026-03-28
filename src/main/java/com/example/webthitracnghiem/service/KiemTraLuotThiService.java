package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.model.DeThi;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.PhienThi;
import com.example.webthitracnghiem.repository.DeThiRepository;
import com.example.webthitracnghiem.repository.NguoiDungRepository;
import com.example.webthitracnghiem.repository.PhienThiRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Kiểm tra và enforce giới hạn số lần thi.
 *
 * Quy tắc:
 * - Chỉ phiên đã nộp (thoiGianNop != null) mới tính vào lượt thi.
 * - Phiên đang dở (thoiGianBatDau != null, thoiGianNop == null) được phép resume,
 *   không tăng thêm lượt.
 *
 * Khi soLanThiToiDa = 1:
 *   Lần 1: bắt đầu → phiên dở → thoát / mất mạng → vào lại (resume cùng phiên)
 *   Lần 1 nộp bài → thoiGianNop != null → đã dùng 1 lượt
 *   Lần 2 bắt đầu → BỊ CHẶN vì đã đếm 1 lượt (lần nộp)
 */
@Service
public class KiemTraLuotThiService {

    private final PhienThiRepository phienThiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final DeThiRepository deThiRepository;

    public KiemTraLuotThiService(
            PhienThiRepository phienThiRepository,
            NguoiDungRepository nguoiDungRepository,
            DeThiRepository deThiRepository) {
        this.phienThiRepository = phienThiRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.deThiRepository = deThiRepository;
    }

    /**
     * Kết quả kiểm tra lượt thi.
     *
     * @param phienThiId   ID phiên để resume (nếu loai == IN_PROGRESS), hoặc null
     * @param loai        ALLOWED / IN_PROGRESS / LIMIT_REACHED / NOT_FOUND / ERROR
     * @param thongBao    Thông điệp mô tả
     */
    public record KetQuaLuotThi(
            String phienThiId,
            LoaiKetQua loai,
            String thongBao) {

        public enum LoaiKetQua {
            /** Được phép bắt đầu / tiếp tục phiên */
            ALLOWED,
            /** Có phiên đang dở — trả về phienThiId để client resume */
            IN_PROGRESS,
            /** Đã dùng hết lượt, không cho thi thêm */
            LIMIT_REACHED,
            /** Không tìm thấy đề thi hoặc người dùng */
            NOT_FOUND,
            /** Lỗi hệ thống */
            ERROR
        }
    }

    /**
     * Kiểm tra xem sinh viên có được phép bắt đầu / tiếp tục thi.
     *
     * GỌI METHOD NÀY tại điểm tạo phiên thi (khi sinh viên bấm "Bắt đầu thi").
     * Nếu trả về ALLOWED → tạo PhienThi mới.
     * Nếu trả về IN_PROGRESS → dùng phienThiId để resume (không tạo mới).
     * Nếu trả về LIMIT_REACHED → báo lỗi cho sinh viên.
     *
     * @param sinhVienId  ID sinh viên
     * @param deThiId     ID đề thi
     * @return KetQuaLuotThi
     */
    public KetQuaLuotThi kiemTra(String sinhVienId, String deThiId) {
        try {
            Optional<NguoiDung> optSV = nguoiDungRepository.findById(sinhVienId);
            if (optSV.isEmpty()) {
                return new KetQuaLuotThi(null, KetQuaLuotThi.LoaiKetQua.NOT_FOUND,
                        "Không tìm thấy tài khoản sinh viên.");
            }

            Optional<DeThi> optDe = deThiRepository.findByIdAndNotDeleted(deThiId);
            if (optDe.isEmpty()) {
                return new KetQuaLuotThi(null, KetQuaLuotThi.LoaiKetQua.NOT_FOUND,
                        "Không tìm thấy đề thi.");
            }

            DeThi deThi = optDe.get();
            NguoiDung sinhVien = optSV.get();

            // Kiểm tra phiên đang dở (resume)
            Optional<PhienThi> optPhienDangDo =
                    phienThiRepository.timPhienDangDo(sinhVien, deThi);
            if (optPhienDangDo.isPresent()) {
                return new KetQuaLuotThi(
                        optPhienDangDo.get().getId(),
                        KetQuaLuotThi.LoaiKetQua.IN_PROGRESS,
                        "Bạn có phiên đang dở. Hệ thống sẽ tiếp tục phiên cũ.");
            }

            // Không giới hạn lượt
            if (deThi.getSoLanThiToiDa() == null) {
                return new KetQuaLuotThi(null, KetQuaLuotThi.LoaiKetQua.ALLOWED, null);
            }

            // Đếm phiên đã nộp
            long soLanDaNop = phienThiRepository.demSoLanDaNop(sinhVien, deThi);
            if (soLanDaNop >= deThi.getSoLanThiToiDa()) {
                return new KetQuaLuotThi(null,
                        KetQuaLuotThi.LoaiKetQua.LIMIT_REACHED,
                        "Bạn đã sử dụng hết " + deThi.getSoLanThiToiDa() + " lượt thi của đề này.");
            }

            return new KetQuaLuotThi(null, KetQuaLuotThi.LoaiKetQua.ALLOWED, null);

        } catch (Exception e) {
            return new KetQuaLuotThi(null, KetQuaLuotThi.LoaiKetQua.ERROR,
                    "Lỗi hệ thống khi kiểm tra lượt thi: " + e.getMessage());
        }
    }
}
