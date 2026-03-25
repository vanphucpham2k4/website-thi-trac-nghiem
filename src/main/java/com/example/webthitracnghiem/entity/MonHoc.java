package com.example.webthitracnghiem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mon_hoc")
@Getter
@Setter
@NoArgsConstructor
public class MonHoc {

	@Id
	@Column(length = 36)
	private String id;

	private String ten;

	@Column(name = "mo_ta", columnDefinition = "TEXT")
	private String moTa;
}
