package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.PhanThuong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhanThuongRepository extends JpaRepository<PhanThuong, String> {

    List<PhanThuong> findByHienThiTrueOrderByThuTuAscDiemDoiAsc();
}
