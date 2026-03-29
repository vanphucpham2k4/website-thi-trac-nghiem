package com.example.webthitracnghiem.repository;

import com.example.webthitracnghiem.model.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository - Truy xuất dữ liệu bảng NGUOI_DUNG (Người Dùng)
 * JpaRepository cung cấp sẵn các phương thức CRUD cơ bản
 * Thêm các phương thức tìm kiếm tùy chỉnh theo nhu cầu
 */
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {

    /**
     * Tìm người dùng theo EMAIL
     * @param email Email cần tìm
     * @return Optional chứa người dùng nếu tìm thấy, empty nếu không
     */
    Optional<NguoiDung> findByEmail(String email);

    /**
     * Tìm người dùng theo SỐ ĐIỆN THOẠI
     * @param soDienThoai Số điện thoại cần tìm
     * @return Optional chứa người dùng nếu tìm thấy, empty nếu không
     */
    Optional<NguoiDung> findBySoDienThoai(String soDienThoai);

    /**
     * Kiểm tra EMAIL đã tồn tại trong hệ thống chưa
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại, false nếu chưa
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra SỐ ĐIỆN THOẠI đã tồn tại trong hệ thống chưa
     * @param soDienThoai Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã tồn tại, false nếu chưa
     */
    boolean existsBySoDienThoai(String soDienThoai);

    /**
     * Tìm người dùng theo EMAIL HOẶC SỐ ĐIỆN THOẠI
     * Dùng cho chức năng đăng nhập: hỗ trợ đăng nhập bằng email hoặc sdt
     * @param email Email cần tìm
     * @param soDienThoai Số điện thoại cần tìm
     * @return Optional chứa người dùng nếu tìm thấy
     */
    @Query("SELECT nd FROM NguoiDung nd WHERE nd.email = :email OR nd.soDienThoai = :soDienThoai")
    Optional<NguoiDung> findByEmailOrSoDienThoai(
            @Param("email") String email,
            @Param("soDienThoai") String soDienThoai
    );

    /**
     * Tìm người dùng theo EMAIL và MẬT KHẨU
     * Dùng cho chức năng đăng nhập
     * @param email Email đăng nhập
     * @param matKhau Mật khẩu đăng nhập
     * @return Optional chứa người dùng nếu thông tin khớp
     */
    @Query("SELECT nd FROM NguoiDung nd WHERE (nd.email = :taiKhoan OR nd.soDienThoai = :taiKhoan) AND nd.matKhau = :matKhau")
    Optional<NguoiDung> findByTaiKhoanAndMatKhau(
            @Param("taiKhoan") String taiKhoan,
            @Param("matKhau") String matKhau
    );

    @Query("SELECT nd FROM NguoiDung nd JOIN NguoiDungVaiTro ndvt ON ndvt.nguoiDung = nd WHERE ndvt.vaiTro.tenVaiTro = :tenVaiTro")
    List<NguoiDung> findByVaiTro(@Param("tenVaiTro") String tenVaiTro);
}
