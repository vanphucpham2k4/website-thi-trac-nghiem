package com.example.webthitracnghiem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ket_qua_thi")
@Getter
@Setter
@NoArgsConstructor
public class KetQuaThi {

	@Id
	@Column(length = 36)
	private String id;

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDphien_thi", nullable = false, unique = true)
	private PhienThi phienThi;

	@Column(name = "tong_diem", precision = 10, scale = 2)
	private BigDecimal tongDiem;

	@Column(name = "thoi_gian_nop")
	private LocalDateTime thoiGianNop;

	@Column(name = "trang_thai_cham")
	private String trangThaiCham;
}
