package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.CauTraLoi;
import com.example.webthitracnghiem.model.PhienThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CauTraLoiRepository extends JpaRepository<CauTraLoi, String> {

    void deleteByPhienThi(PhienThi phienThi);
}
