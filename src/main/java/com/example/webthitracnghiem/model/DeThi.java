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
@Table(name = "de_thi")
@Getter
@Setter
@NoArgsConstructor
public class DeThi {

	@Id
	@Column(length = 36)
	private String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDmon_hoc", nullable = false)
	private MonHoc monHoc;

	@Column(name = "ma_de_thi")
	private String maDeThi;

	private String ten;

	@Column(name = "thoi_gian_phut")
	private Integer thoiGianPhut;

	@Column(name = "mo_ta", columnDefinition = "TEXT")
	private String moTa;

	@Column(name = "ma_truy_cap")
	private String maTruyCap;

	@Column(name = "duong_dan_truy_cap")
	private String duongDanTruyCap;

	@Column(name = "thoi_gian_mo")
	private LocalDateTime thoiGianMo;

	@Column(name = "thoi_gian_dong")
	private LocalDateTime thoiGianDong;

	@Column(name = "so_lan_thi_toi_da")
	private Integer soLanThiToiDa;

	@Column(name = "tron_cau_hoi")
	private Boolean tronCauHoi;

	@Column(name = "tron_dap_an")
	private Boolean tronDapAn;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "IDnguoi_dung", nullable = false)
	private NguoiDung nguoiDung;

	/**
	 * Trạng thái đề thi: NHAP (nháp, chưa công bố) hoặc CONG_KHAI (công khai cho sinh viên thi)
	 * Mặc định là NHAP khi tạo mới
	 */
	@Column(name = "trang_thai", length = 20)
	private String trangThai = "NHAP";

	/**
	 * Thời điểm xóa mềm — null có nghĩa đề thi chưa bị xóa.
	 * Dùng cho Soft Delete: đánh dấu đề đã xóa mà không mất dữ liệu.
	 */
	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	/**
	 * Thời điểm tạo đề thi — dùng để sắp xếp và hiển thị thông tin.
	 */
	@Column(name = "thoi_gian_tao")
	private LocalDateTime thoiGianTao;
}
