package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.entity.NguoiDungVaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository - Truy xuất dữ liệu bảng NGUOI_DUNG_VAI_TRO (Người Dùng Vai Trò)
 * JpaRepository cung cấp sẵn các phương thức CRUD cơ bản
 * Lưu trữ thông tin người dùng thuộc vai trò nào
 */
@Repository
public interface NguoiDungVaiTroRepository extends JpaRepository<NguoiDungVaiTro, String> {

    /**
     * Tìm tất cả vai trò của một người dùng
     * @param nguoiDungId ID của người dùng cần tìm vai trò
     * @return Danh sách các vai trò của người dùng đó
     */
    List<NguoiDungVaiTro> findByNguoiDungId(String nguoiDungId);

    long countByVaiTro_TenVaiTro(String tenVaiTro);
}
