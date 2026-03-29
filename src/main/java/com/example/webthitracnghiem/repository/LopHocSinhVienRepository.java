package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.LopHoc;
import com.example.webthitracnghiem.model.LopHocSinhVien;
import com.example.webthitracnghiem.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LopHocSinhVienRepository extends JpaRepository<LopHocSinhVien, String> {

    List<LopHocSinhVien> findByLopHoc(LopHoc lopHoc);

    void deleteByLopHoc(LopHoc lopHoc);

    boolean existsByLopHocAndSinhVien(LopHoc lopHoc, NguoiDung sinhVien);

    List<LopHocSinhVien> findBySinhVien(NguoiDung sinhVien);

    long countByLopHoc(LopHoc lopHoc);
}
