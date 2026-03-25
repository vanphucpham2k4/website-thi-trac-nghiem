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

@Entity
@Table(name = "de_thi_cau_hoi")
@Getter
@Setter
@NoArgsConstructor
public class DeThiCauHoi {

	@Id
	@Column(length = 36)
	private String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDde_thi", nullable = false)
	private DeThi deThi;

	@Column(name = "thu_tu")
	private Integer thuTu;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDcau_hoi", nullable = false)
	private CauHoi cauHoi;
}
