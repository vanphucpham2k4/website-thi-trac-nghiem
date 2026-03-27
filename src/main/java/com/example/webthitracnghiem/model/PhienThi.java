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

@Entity
@Table(name = "phien_thi")
@Getter
@Setter
@NoArgsConstructor
public class PhienThi {

	@Id
	@Column(length = 36)
	private String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDde_thi", nullable = false)
	private DeThi deThi;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDnguoi_dung", nullable = false)
	private NguoiDung nguoiDung;

	@Column(name = "ma_truy_cap_da_dung")
	private String maTruyCapDaDung;

	@Column(name = "thoi_gian_bat_dau")
	private LocalDateTime thoiGianBatDau;

	@Column(name = "thoi_gian_nop")
	private LocalDateTime thoiGianNop;

	@Column(name = "trang_thai")
	private String trangThai;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDcau_hoi_hien_tai")
	private CauHoi cauHoiHienTai;
}
