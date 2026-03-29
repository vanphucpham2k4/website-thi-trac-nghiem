package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.LopHoc;
import com.example.webthitracnghiem.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LopHocRepository extends JpaRepository<LopHoc, String> {

    List<LopHoc> findByGiaoVienOrderByThoiGianTaoDesc(NguoiDung giaoVien);

    Optional<LopHoc> findByIdAndGiaoVien(String id, NguoiDung giaoVien);
}
