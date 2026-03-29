package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.CauHoi;
import com.example.webthitracnghiem.model.DeThi;
import com.example.webthitracnghiem.model.DeThiCauHoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeThiCauHoiRepository extends JpaRepository<DeThiCauHoi, String> {

    void deleteByDeThi(DeThi deThi);

    void deleteByCauHoi(CauHoi cauHoi);

    /** Đếm số câu hỏi trong một đề thi */
    long countByDeThi(DeThi deThi);

    /** Lấy danh sách câu hỏi trong đề, sắp xếp theo thứ tự */
    List<DeThiCauHoi> findByDeThiOrderByThuTuAsc(DeThi deThi);

    /** Kiểm tra câu hỏi đã có trong đề chưa (tránh trùng) */
    boolean existsByDeThiAndCauHoi(DeThi deThi, CauHoi cauHoi);

    /** Tìm liên kết đề - câu hỏi theo đề và câu hỏi */
    Optional<DeThiCauHoi> findByDeThiAndCauHoi(DeThi deThi, CauHoi cauHoi);

    /** Xóa một câu hỏi khỏi đề theo đề và câu hỏi */
    @Modifying
    @Query("DELETE FROM DeThiCauHoi dc WHERE dc.deThi = :deThi AND dc.cauHoi = :cauHoi")
    void deleteByDeThiAndCauHoi(@Param("deThi") DeThi deThi, @Param("cauHoi") CauHoi cauHoi);

    /** Lấy số thứ tự lớn nhất trong đề (để append câu mới vào cuối) */
    @Query("SELECT COALESCE(MAX(dc.thuTu), 0) FROM DeThiCauHoi dc WHERE dc.deThi = :deThi")
    int layThuTuLonNhat(@Param("deThi") DeThi deThi);
}
