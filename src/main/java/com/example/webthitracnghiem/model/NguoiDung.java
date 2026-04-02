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

	/**
	 * Điểm thưởng tích lũy từ kết quả thi (tổng {@code tong_diem} các bài đã nộp, làm tròn xuống int).
	 * Được đồng bộ từ bảng {@code ket_qua_thi}, không tự cộng tay ngoài luồng thi.
	 */
	@Column(name = "diem_thuong_tich_luy", nullable = false)
	private int diemThuongTichLuy;

	/**
	 * Số lượt đổi thưởng thành công (trạng thái đã duyệt hoặc đã nhận quà).
	 */
	@Column(name = "tong_luot_doi_thanh_cong", nullable = false)
	private long tongLuotDoiThanhCong;
}
