package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.ChuDe;
import com.example.webthitracnghiem.model.MonHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository - Truy xuất dữ liệu bảng CHU_DE (Chủ Đề)
 * Mỗi chủ đề thuộc một môn học, dùng để phân loại câu hỏi trong ngân hàng câu hỏi.
 */
@Repository
public interface ChuDeRepository extends JpaRepository<ChuDe, String> {

    /**
     * Tìm tất cả chủ đề thuộc một môn học cụ thể
     * @param monHoc Môn học cần lấy chủ đề
     * @return Danh sách chủ đề
     */
    List<ChuDe> findByMonHoc(MonHoc monHoc);

    List<ChuDe> findByMonHocOrderByIdAsc(MonHoc monHoc);

    long countByMonHoc(MonHoc monHoc);

    /**
     * Kiểm tra chủ đề đã tồn tại trong môn học chưa (tránh trùng tên)
     * @param ten Tên chủ đề
     * @param monHoc Môn học
     * @return true nếu đã tồn tại
     */
    boolean existsByTenAndMonHoc(String ten, MonHoc monHoc);

    List<ChuDe> findByMonHocId(String monHocId);
}
