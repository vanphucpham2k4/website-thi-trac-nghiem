package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.KetQuaThi;
import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.PhienThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository - Truy xuất dữ liệu bảng KET_QUA_THI (Kết Quả Thi)
 * JpaRepository cung cấp sẵn các phương thức CRUD cơ bản
 */
@Repository
public interface KetQuaThiRepository extends JpaRepository<KetQuaThi, String> {

    Optional<KetQuaThi> findByPhienThi(PhienThi phienThi);

    /**
     * Tìm kết quả thi theo người dùng
     * @param nguoiDung Người dùng cần tìm kết quả
     * @return Danh sách kết quả thi
     */
    @Query("SELECT kq FROM KetQuaThi kq " +
           "JOIN FETCH kq.phienThi pt " +
           "JOIN FETCH pt.deThi d " +
           "LEFT JOIN FETCH d.monHoc " +
           "WHERE pt.nguoiDung = :nguoiDung " +
           "ORDER BY kq.thoiGianNop DESC")
    List<KetQuaThi> findByNguoiDung(@Param("nguoiDung") NguoiDung nguoiDung);

    /**
     * Tính điểm trung bình của một người dùng
     * @param nguoiDung Người dùng
     * @return Điểm trung bình (BigDecimal)
     */
    @Query("SELECT AVG(kq.tongDiem) FROM KetQuaThi kq WHERE kq.phienThi.nguoiDung = :nguoiDung")
    BigDecimal tinhDiemTrungBinh(@Param("nguoiDung") NguoiDung nguoiDung);

    /**
     * Đếm số bài thi đã hoàn thành của một người dùng
     * @param nguoiDung Người dùng
     * @return Số bài thi đã nộp
     */
    @Query("SELECT COUNT(kq) FROM KetQuaThi kq WHERE kq.phienThi.nguoiDung = :nguoiDung")
    long demSoBaiThiHoanThanh(@Param("nguoiDung") NguoiDung nguoiDung);

    /**
     * Tổng điểm bài thi (tổng điểm) của sinh viên — dùng làm nguồn điểm thưởng đổi quà (làm tròn xuống int).
     */
    @Query("SELECT COALESCE(SUM(kq.tongDiem), 0) FROM KetQuaThi kq WHERE kq.phienThi.nguoiDung = :nguoiDung")
    BigDecimal tongDiemTichLuy(@Param("nguoiDung") NguoiDung nguoiDung);

    /**
     * Lấy kết quả thi theo đề thi + lớp, kèm cả kết quả ẩn danh (lopHoc IS NULL).
     */
    @Query("SELECT kq FROM KetQuaThi kq JOIN FETCH kq.phienThi pt " +
           "LEFT JOIN FETCH pt.nguoiDung LEFT JOIN FETCH pt.lopHoc " +
           "WHERE pt.deThi.id = :deThiId AND (pt.lopHoc.id = :lopHocId OR pt.lopHoc IS NULL) " +
           "ORDER BY kq.thoiGianNop DESC")
    List<KetQuaThi> findByDeThiIdAndLopHocIdOrAnonymous(@Param("deThiId") String deThiId,
                                                         @Param("lopHocId") String lopHocId);

    /**
     * Lấy TẤT CẢ kết quả thi theo đề thi (mọi lớp + ẩn danh).
     * Dùng cho luồng "Tất cả đề thi" khi giáo viên không chọn lớp.
     */
    @Query("SELECT kq FROM KetQuaThi kq JOIN FETCH kq.phienThi pt " +
           "LEFT JOIN FETCH pt.nguoiDung LEFT JOIN FETCH pt.lopHoc " +
           "WHERE pt.deThi.id = :deThiId " +
           "ORDER BY kq.thoiGianNop DESC")
    List<KetQuaThi> findAllByDeThiId(@Param("deThiId") String deThiId);
}
