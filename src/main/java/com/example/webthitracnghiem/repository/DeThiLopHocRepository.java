package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.DeThi;
import com.example.webthitracnghiem.model.DeThiLopHoc;
import com.example.webthitracnghiem.model.LopHoc;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
