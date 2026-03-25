package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository - Truy xuất dữ liệu bảng VAI_TRO (Vai Trò)
 * JpaRepository cung cấp sẵn các phương thức CRUD cơ bản
 * Cung cấp các phương thức tìm kiếm theo tên vai trò
 */
@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, String> {

    /**
     * Tìm vai trò theo TÊN VAI TRÒ
     * @param tenVaiTro Tên vai trò cần tìm (ví dụ: "ADMIN", "GIAO_VIEN", "SINH_VIEN")
     * @return Optional chứa vai trò nếu tìm thấy, empty nếu không
     */
    Optional<VaiTro> findByTenVaiTro(String tenVaiTro);

    /**
     * Kiểm tra vai trò đã tồn tại theo tên chưa
     * @param tenVaiTro Tên vai trò cần kiểm tra
     * @return true nếu vai trò đã tồn tại, false nếu chưa
     */
    boolean existsByTenVaiTro(String tenVaiTro);
}
