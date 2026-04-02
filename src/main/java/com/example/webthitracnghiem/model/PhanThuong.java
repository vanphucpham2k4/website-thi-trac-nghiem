package com.example.webthitracnghiem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "phan_thuong")
@Getter
@Setter
@NoArgsConstructor
public class PhanThuong {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String ten;

    @Column(name = "mo_ta_ngan", length = 600)
    private String moTaNgan;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false, length = 40)
    private LoaiPhanThuong loai;

    @Column(name = "diem_doi", nullable = false)
    private int diemDoi;

    @Column(name = "so_luong_con_lai", nullable = false)
    private int soLuongConLai;

    /** Font Awesome class, ví dụ: fas fa-medal */
    @Column(name = "icon_class", length = 80)
    private String iconClass;

    /** URL ảnh minh họa thẻ phần thưởng (https, CDN…) */
    @Column(name = "anh_url", length = 800)
    private String anhUrl;

    @Column(name = "hien_thi", nullable = false)
    private boolean hienThi = true;

    @Column(name = "thu_tu", nullable = false)
    private int thuTu;
}
