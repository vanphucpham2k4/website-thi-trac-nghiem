package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.entity.DeThi;
import com.example.webthitracnghiem.entity.NguoiDung;
import com.example.webthitracnghiem.entity.PhienThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository - Truy xuất dữ liệu bảng PHIEN_THI (Phiên Thi)
 * JpaRepository cung cấp sẵn các phương thức CRUD cơ bản
 */
@Repository
public interface PhienThiRepository extends JpaRepository<PhienThi, String> {

    /**
     * Tìm tất cả phiên thi của một người dùng
     * @param nguoiDung Người dùng tham gia thi
     * @return Danh sách phiên thi
     */
    List<PhienThi> findByNguoiDung(NguoiDung nguoiDung);

    /**
     * Tìm tất cả phiên thi của một đề thi
     * @param deThi Đề thi
     * @return Danh sách phiên thi
     */
    List<PhienThi> findByDeThi(DeThi deThi);

    /**
     * Đếm số lần thi của một người dùng với một đề thi cụ thể
     * @param nguoiDung Người dùng
     * @param deThi Đề thi
     * @return Số lần đã thi
     */
    long countByNguoiDungAndDeThi(NguoiDung nguoiDung, DeThi deThi);

    /**
     * Đếm tổng số lần thi của một người dùng
     * @param nguoiDung Người dùng
     * @return Tổng số lần thi
     */
    @Query("SELECT COUNT(p) FROM PhienThi p WHERE p.nguoiDung = :nguoiDung")
    long demTongSoLanThi(@Param("nguoiDung") NguoiDung nguoiDung);

    /**
     * Tìm phiên thi đang diễn ra (chưa nộp bài)
     * @param nguoiDung Người dùng
     * @param trangThai Trạng thái đang thi
     * @return Danh sách phiên thi đang thi
     */
    List<PhienThi> findByNguoiDungAndTrangThai(NguoiDung nguoiDung, String trangThai);

    List<PhienThi> findByDeThiId(String deThiId);
}
