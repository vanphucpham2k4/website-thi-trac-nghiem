package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.entity.DeThi;
import com.example.webthitracnghiem.entity.MonHoc;
import com.example.webthitracnghiem.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository - Truy xuất dữ liệu bảng DE_THI (Đề Thi)
 * JpaRepository cung cấp sẵn các phương thức CRUD cơ bản
 */
@Repository
public interface DeThiRepository extends JpaRepository<DeThi, String> {

    /**
     * Tìm tất cả đề thi của một giáo viên
     * @param nguoiDung Giáo viên tạo đề thi
     * @return Danh sách đề thi
     */
    List<DeThi> findByNguoiDung(NguoiDung nguoiDung);

    /**
     * Tìm đề thi theo mã truy cập
     * @param maTruyCap Mã truy cập đề thi
     * @return Optional chứa đề thi nếu tìm thấy
     */
    Optional<DeThi> findByMaTruyCap(String maTruyCap);

    /**
     * Tìm đề thi theo môn học
     * @param monHoc Môn học cần tìm
     * @return Danh sách đề thi thuộc môn học đó
     */
    List<DeThi> findByMonHoc(MonHoc monHoc);

    /**
     * Đếm tổng số đề thi trong hệ thống
     * @return Số lượng đề thi
     */
    @Query("SELECT COUNT(d) FROM DeThi d")
    long demTongSoDeThi();

    /**
     * Đếm số đề thi của một giáo viên
     * @param nguoiDung Giáo viên cần đếm
     * @return Số lượng đề thi
     */
    long countByNguoiDung(NguoiDung nguoiDung);

    // ================================================================
    // SOFT DELETE AWARE QUERIES — chỉ lấy đề thi chưa bị xóa mềm
    // ================================================================

    /**
     * Lấy tất cả đề thi chưa bị xóa của một giáo viên (sắp xếp theo thời gian tạo mới nhất)
     */
    List<DeThi> findByNguoiDungAndDeletedAtIsNullOrderByThoiGianTaoDesc(NguoiDung nguoiDung);

    /**
     * Lấy tất cả đề thi đã bị xóa mềm của một giáo viên
     */
    List<DeThi> findByNguoiDungAndDeletedAtIsNotNull(NguoiDung nguoiDung);

    /**
     * Đếm số đề thi chưa xóa của một giáo viên
     */
    long countByNguoiDungAndDeletedAtIsNull(NguoiDung nguoiDung);

    /**
     * Đếm số đề thi theo trạng thái (NHAP/CONG_KHAI) của giáo viên (chưa xóa)
     */
    long countByNguoiDungAndTrangThaiAndDeletedAtIsNull(NguoiDung nguoiDung, String trangThai);

    /**
     * Tìm đề thi chưa xóa theo ID (an toàn hơn findById thông thường)
     */
    @Query("SELECT d FROM DeThi d WHERE d.id = :id AND d.deletedAt IS NULL")
    java.util.Optional<DeThi> findByIdAndNotDeleted(@Param("id") String id);
}
