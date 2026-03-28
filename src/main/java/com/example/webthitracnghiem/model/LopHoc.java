package com.example.webthitracnghiem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lớp học do giáo viên tạo, gắn danh sách sinh viên qua bảng lop_hoc_sinh_vien.
 */
@Entity
@Table(name = "lop_hoc")
@Getter
@Setter
@NoArgsConstructor
public class LopHoc {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String ten;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDgiao_vien", nullable = false)
    private NguoiDung giaoVien;

    @Column(name = "thoi_gian_tao")
    private LocalDateTime thoiGianTao;
}
