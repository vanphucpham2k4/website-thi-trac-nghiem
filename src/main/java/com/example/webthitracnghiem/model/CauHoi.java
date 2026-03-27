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

	/**
	 * Các lựa chọn đáp án cho câu hỏi trắc nghiệm (MCQ).
	 * Với câu hỏi dạng khác (đúng/sai, tự luận), có thể để null.
	 * dapAnDung lưu chữ cái đúng: "A", "B", "C", hoặc "D"
	 */
	@Column(name = "lua_chon_a", columnDefinition = "TEXT")
	private String luaChonA;

	@Column(name = "lua_chon_b", columnDefinition = "TEXT")
	private String luaChonB;

	@Column(name = "lua_chon_c", columnDefinition = "TEXT")
	private String luaChonC;

	@Column(name = "lua_chon_d", columnDefinition = "TEXT")
	private String luaChonD;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDnguoi_dung", nullable = false)
	private NguoiDung nguoiDung;
}
