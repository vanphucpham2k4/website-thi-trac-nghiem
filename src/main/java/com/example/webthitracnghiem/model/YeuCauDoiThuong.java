package com.example.webthitracnghiem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "yeu_cau_doi_thuong")
@Getter
@Setter
@NoArgsConstructor
public class YeuCauDoiThuong {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "ma_doi", nullable = false, unique = true, length = 48)
    private String maDoi;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDnguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDphan_thuong", nullable = false)
    private PhanThuong phanThuong;

    @Column(name = "diem_da_dung", nullable = false)
    private int diemDaDung;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 30)
    private TrangThaiYeuCauDoiThuong trangThai;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;
}
