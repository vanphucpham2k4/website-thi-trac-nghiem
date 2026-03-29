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

import java.time.LocalDateTime;

/**
 * Đề thi được xuất bản cho một lớp — sinh viên trong lớp mới thấy đề.
 */
@Entity
@Table(
        name = "de_thi_lop_hoc",
        uniqueConstraints = @UniqueConstraint(columnNames = { "IDde_thi", "IDlop_hoc" })
)
@Getter
@Setter
@NoArgsConstructor
public class DeThiLopHoc {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDde_thi", nullable = false)
    private DeThi deThi;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDlop_hoc", nullable = false)
    private LopHoc lopHoc;

    @Column(name = "thoi_gian_xuat_ban")
    private LocalDateTime thoiGianXuatBan;
}
