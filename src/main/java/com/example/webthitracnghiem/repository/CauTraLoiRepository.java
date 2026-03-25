package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.entity.CauTraLoi;
import com.example.webthitracnghiem.entity.PhienThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CauTraLoiRepository extends JpaRepository<CauTraLoi, String> {

    void deleteByPhienThi(PhienThi phienThi);
}
