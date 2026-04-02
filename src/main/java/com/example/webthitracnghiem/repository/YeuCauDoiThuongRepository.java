package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.NguoiDung;
import com.example.webthitracnghiem.model.TrangThaiYeuCauDoiThuong;
import com.example.webthitracnghiem.model.YeuCauDoiThuong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface YeuCauDoiThuongRepository extends JpaRepository<YeuCauDoiThuong, String> {

    boolean existsByMaDoi(String maDoi);

    @Query("SELECT y FROM YeuCauDoiThuong y JOIN FETCH y.nguoiDung JOIN FETCH y.phanThuong ORDER BY y.thoiGian DESC")
    List<YeuCauDoiThuong> findAllForAdminOrderByThoiGianDesc();

    @Query("SELECT y FROM YeuCauDoiThuong y JOIN FETCH y.nguoiDung JOIN FETCH y.phanThuong WHERE y.id = :id")
    Optional<YeuCauDoiThuong> findByIdForAdmin(@Param("id") String id);

    @Query("SELECT y FROM YeuCauDoiThuong y JOIN FETCH y.phanThuong WHERE y.nguoiDung.id = :uid ORDER BY y.thoiGian DESC")
    List<YeuCauDoiThuong> findByNguoiDungIdWithPhanThuong(@Param("uid") String nguoiDungId);

    @Query("SELECT COALESCE(SUM(y.diemDaDung), 0) FROM YeuCauDoiThuong y WHERE y.nguoiDung = :nd AND y.trangThai <> :daHuy")
    long sumDiemDaDungChuaHuy(@Param("nd") NguoiDung nd, @Param("daHuy") TrangThaiYeuCauDoiThuong daHuy);

    long countByNguoiDungAndTrangThaiIn(NguoiDung nguoiDung, Collection<TrangThaiYeuCauDoiThuong> trangThais);

    @Query("SELECT y FROM YeuCauDoiThuong y JOIN FETCH y.phanThuong WHERE y.id = :id AND y.nguoiDung.id = :uid")
    Optional<YeuCauDoiThuong> findByIdAndNguoiDungIdWithPhanThuong(@Param("id") String id, @Param("uid") String uid);
}
