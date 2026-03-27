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

    Optional<DeThi> findByIdAndNguoiDung(String id, NguoiDung nguoiDung);

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
}
