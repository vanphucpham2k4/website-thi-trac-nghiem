package com.example.webthitracnghiem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
public class NguoiDung {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "ma_nguoi_dung")
	private String maNguoiDung;

	private String ho;

	private String ten;

	@Column(name = "ho_ten")
	private String hoTen;

	private String email;

	@Column(name = "so_dien_thoai")
	private String soDienThoai;

	@Column(name = "mat_khau")
	private String matKhau;
}
