package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.CauHoi;
import com.example.webthitracnghiem.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CauHoiRepository extends JpaRepository<CauHoi, String> {

    List<CauHoi> findByNguoiDung(NguoiDung nguoiDung);
}
