package com.example.webthitracnghiem.entity;

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

import java.math.BigDecimal;

@Entity
@Table(name = "cau_tra_loi")
@Getter
@Setter
@NoArgsConstructor
public class CauTraLoi {

	@Id
	@Column(length = 36)
	private String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDcau_hoi", nullable = false)
	private CauHoi cauHoi;

	@Column(name = "noi_dung_tra_loi", columnDefinition = "TEXT")
	private String noiDungTraLoi;

	@Column(name = "trang_thai_tra_loi")
	private String trangThaiTraLoi;

	@Column(name = "tu_dong_cham")
	private Boolean tuDongCham;

	@Column(precision = 10, scale = 2)
	private BigDecimal diem;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDphien_thi", nullable = false)
	private PhienThi phienThi;
}
