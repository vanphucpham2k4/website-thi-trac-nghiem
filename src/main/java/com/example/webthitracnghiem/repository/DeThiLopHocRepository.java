package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.DeThi;
import com.example.webthitracnghiem.model.DeThiLopHoc;
import com.example.webthitracnghiem.model.LopHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeThiLopHocRepository extends JpaRepository<DeThiLopHoc, String> {

    List<DeThiLopHoc> findByLopHocOrderByThoiGianXuatBanDesc(LopHoc lopHoc);

    Optional<DeThiLopHoc> findByDeThiAndLopHoc(DeThi deThi, LopHoc lopHoc);

    List<DeThiLopHoc> findByDeThi(DeThi deThi);

    void deleteByDeThi(DeThi deThi);

    void deleteByLopHoc(LopHoc lopHoc);

    boolean existsByDeThiAndLopHoc(DeThi deThi, LopHoc lopHoc);

    long countByLopHoc(LopHoc lopHoc);

    /**
     * Lấy danh sách đề xuất bản cho lớp, kèm fetch đề thi + môn học (giáo viên xem kết quả).
     */
    @Query("SELECT dtlh FROM DeThiLopHoc dtlh JOIN FETCH dtlh.deThi dt " +
           "LEFT JOIN FETCH dt.monHoc WHERE dtlh.lopHoc = :lopHoc " +
           "ORDER BY dtlh.thoiGianXuatBan DESC")
    List<DeThiLopHoc> findByLopHocFetchDeThi(@Param("lopHoc") LopHoc lopHoc);
}
