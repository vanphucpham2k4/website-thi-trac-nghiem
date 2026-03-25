package com.example.webthitracnghiem.dto;

/**
 * DTO - Đối tượng chứa thông tin CAPTCHA trả về cho frontend
 * Lưu trữ cặp câu hỏi - đáp án để xác thực người dùng là con người
 */
public class CaptchaDTO {

    /**
     * ID duy nhất của CAPTCHA - dùng để xác thực khi submit form
     * Sẽ được lưu trong session hoặc cache phía server
     */
    private String captchaId;

    /**
     * Câu hỏi CAPTCHA hiển thị cho người dùng
     * Format: "X + Y = ?" hoặc "X - Y = ?" (ví dụ: "2 + 3 = ?")
     */
    private String captchaQuestion;

    /**
     * Constructor mặc định
     */
    public CaptchaDTO() {
    }

    /**
     * Constructor với đầy đủ tham số
     * @param captchaId ID của CAPTCHA
     * @param captchaQuestion Câu hỏi hiển thị
     */
    public CaptchaDTO(String captchaId, String captchaQuestion) {
        this.captchaId = captchaId;
        this.captchaQuestion = captchaQuestion;
    }

    // ===== GETTER và SETTER =====

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaQuestion() {
        return captchaQuestion;
    }

    public void setCaptchaQuestion(String captchaQuestion) {
        this.captchaQuestion = captchaQuestion;
    }
}
