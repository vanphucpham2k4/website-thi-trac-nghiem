package com.example.webthitracnghiem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vai_tro")
@Getter
@Setter
@NoArgsConstructor
public class VaiTro {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "ten_vai_tro")
	private String tenVaiTro;

	@Column(name = "mo_ta", columnDefinition = "TEXT")
	private String moTa;
}
