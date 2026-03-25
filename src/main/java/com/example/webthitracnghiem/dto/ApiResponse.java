package com.example.webthitracnghiem.dto;

/**
 * DTO - Đối tượng phản hồi API chuẩn (Standard API Response)
 * Sử dụng để trả về kết quả cho frontend theo định dạng统一
 * @param <T> Kiểu dữ liệu của thuộc tính "data"
 */
public class ApiResponse<T> {

    /**
     * Trạng thái kết quả
     * - true: Thành công
     * - false: Thất bại
     */
    private boolean success;

    /**
     * Thông điệp mô tả kết quả (dành cho người dùng đọc)
     * Ví dụ: "Đăng nhập thành công", "Sai mật khẩu", "Tài khoản không tồn tại"
     */
    private String message;

    /**
     * Dữ liệu trả về (nếu có)
     * Có thể chứa object người dùng, token, v.v.
     */
    private T data;

    /**
     * Mã lỗi chi tiết (nếu thất bại)
     * 0: Không có lỗi
     * 1: Tài khoản không tồn tại
     * 2: Sai mật khẩu
     * 3: CAPTCHA sai
     * 4: Email đã tồn tại
     * 5: Số điện thoại đã tồn tại
     * 6: Vai trò không hợp lệ
     * 7: Lỗi hệ thống
     * 8: Dữ liệu không hợp lệ
     */
    private int errorCode;

    /**
     * Constructor mặc định - Khởi tạo đối tượng rỗng
     */
    public ApiResponse() {
    }

    /**
     * Constructor với tham số - Tạo phản hồi nhanh
     * @param success Trạng thái thành công/thất bại
     * @param message Thông điệp mô tả
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.errorCode = success ? 0 : 7; // 0 = OK, 7 = Lỗi hệ thống mặc định
    }

    /**
     * Constructor đầy đủ - Tạo phản hồi với tất cả thông tin
     * @param success Trạng thái thành công/thất bại
     * @param message Thông điệp mô tả
     * @param data Dữ liệu trả về
     * @param errorCode Mã lỗi
     */
    public ApiResponse(boolean success, String message, T data, int errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    // ===== PHƯƠNG THỨC TIỆN ÍCH =====
    // Các phương thức static để tạo nhanh các loại phản hồi thường dùng

    /**
     * Tạo phản hồi thành công với thông điệp mặc định
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "Thành công");
    }

    /**
     * Tạo phản hồi thành công với thông điệp tùy chỉnh
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    /**
     * Tạo phản hồi thành công với thông điệp và dữ liệu
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, 0);
    }

    /**
     * Tạo phản hồi thất bại với thông điệp mặc định
     */
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(false, "Đã xảy ra lỗi", null, 7);
    }

    /**
     * Tạo phản hồi thất bại với thông điệp tùy chỉnh
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, 7);
    }

    /**
     * Tạo phản hồi thất bại với thông điệp và mã lỗi
     */
    public static <T> ApiResponse<T> error(String message, int errorCode) {
        return new ApiResponse<>(false, message, null, errorCode);
    }

    // ===== GETTER và SETTER =====

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
