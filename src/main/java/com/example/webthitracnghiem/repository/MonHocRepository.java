package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.entity.MonHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository - Truy xuất dữ liệu bảng MON_HOC (Môn Học)
 * JpaRepository cung cấp sẵn các phương thức CRUD cơ bản
 */
@Repository
public interface MonHocRepository extends JpaRepository<MonHoc, String> {

    /**
     * Tìm môn học theo tên
     * @param ten Tên môn học cần tìm
     * @return Optional chứa môn học nếu tìm thấy
     */
    Optional<MonHoc> findByTen(String ten);
}
