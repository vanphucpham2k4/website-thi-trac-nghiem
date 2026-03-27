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

@Entity
@Table(name = "cau_hoi")
@Getter
@Setter
@NoArgsConstructor
public class CauHoi {

	@Id
	@Column(length = 36)
	private String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDchu_de", nullable = false)
	private ChuDe chuDe;

	@Column(name = "noi_dung", columnDefinition = "TEXT")
	private String noiDung;

	@Column(name = "loai_cau_hoi")
	private String loaiCauHoi;

	@Column(name = "do_kho")
	private String doKho;

	@Column(name = "dap_an_dung", columnDefinition = "TEXT")
	private String dapAnDung;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDnguoi_dung", nullable = false)
	private NguoiDung nguoiDung;
}
