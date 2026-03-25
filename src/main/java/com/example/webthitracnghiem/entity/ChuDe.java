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
@Table(name = "chu_de")
@Getter
@Setter
@NoArgsConstructor
public class ChuDe {

	@Id
	@Column(length = 36)
	private String id;

	private String ten;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDmon_hoc", nullable = false)
	private MonHoc monHoc;
}
