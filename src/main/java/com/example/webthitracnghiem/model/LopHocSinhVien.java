package com.example.webthitracnghiem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Thành viên lớp (sinh viên).
 */
@Entity
@Table(
        name = "lop_hoc_sinh_vien",
        uniqueConstraints = @UniqueConstraint(columnNames = { "IDlop_hoc", "IDsinh_vien" })
)
@Getter
@Setter
@NoArgsConstructor
public class LopHocSinhVien {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDlop_hoc", nullable = false)
    private LopHoc lopHoc;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDsinh_vien", nullable = false)
    private NguoiDung sinhVien;
}
