package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.entity.CauHoi;
import com.example.webthitracnghiem.entity.DeThi;
import com.example.webthitracnghiem.entity.DeThiCauHoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeThiCauHoiRepository extends JpaRepository<DeThiCauHoi, String> {

    void deleteByDeThi(DeThi deThi);

    void deleteByCauHoi(CauHoi cauHoi);

    /** Đếm số câu hỏi trong một đề thi */
    long countByDeThi(DeThi deThi);
}
