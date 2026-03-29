package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.ChuDe;
import com.example.webthitracnghiem.model.CauHoi;
import com.example.webthitracnghiem.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository - Truy xuất dữ liệu bảng CAU_HOI (Ngân hàng câu hỏi)
 * Hỗ trợ lọc câu hỏi theo môn học, chủ đề, độ khó và từ khóa tìm kiếm.
 */
@Repository
public interface CauHoiRepository extends JpaRepository<CauHoi, String> {

    List<CauHoi> findByNguoiDung(NguoiDung nguoiDung);

    long countByChuDe(ChuDe chuDe);

    /**
     * Lọc câu hỏi linh hoạt theo nhiều tiêu chí.
     * Các tham số null nghĩa là không lọc theo tiêu chí đó.
     * Dùng JPQL với điều kiện động (truthy null check).
     */
    @Query("""
        SELECT c FROM CauHoi c
        WHERE c.nguoiDung = :nguoiDung
          AND (:monHocId IS NULL OR c.chuDe.monHoc.id = :monHocId)
          AND (:chuDeId  IS NULL OR c.chuDe.id       = :chuDeId)
          AND (:doKho    IS NULL OR c.doKho           = :doKho)
          AND (:keyword  IS NULL OR LOWER(c.noiDung)  LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY c.id DESC
        """)
    List<CauHoi> locCauHoi(
            @Param("nguoiDung") NguoiDung nguoiDung,
            @Param("monHocId")  String monHocId,
            @Param("chuDeId")   String chuDeId,
            @Param("doKho")     String doKho,
            @Param("keyword")   String keyword
    );

    /**
     * Đếm tổng số câu hỏi của một giáo viên
     */
    long countByNguoiDung(NguoiDung nguoiDung);

    /**
     * Kiểm tra câu hỏi có đang được dùng trong đề thi nào không
     * Dùng để quyết định có cho phép xóa hay không
     */
    @Query("SELECT COUNT(dc) FROM DeThiCauHoi dc WHERE dc.cauHoi.id = :cauHoiId")
    long demSoDeThiSuDung(@Param("cauHoiId") String cauHoiId);
}
