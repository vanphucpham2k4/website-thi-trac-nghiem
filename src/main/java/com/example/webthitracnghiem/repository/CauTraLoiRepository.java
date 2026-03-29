package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.CauHoi;
import com.example.webthitracnghiem.model.CauTraLoi;
import com.example.webthitracnghiem.model.PhienThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CauTraLoiRepository extends JpaRepository<CauTraLoi, String> {

    void deleteByPhienThi(PhienThi phienThi);

    List<CauTraLoi> findByPhienThi(PhienThi phienThi);

    Optional<CauTraLoi> findByPhienThiAndCauHoi(PhienThi phienThi, CauHoi cauHoi);
}
