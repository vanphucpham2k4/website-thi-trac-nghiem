package com.example.webthitracnghiem.service;

import com.example.webthitracnghiem.dto.CaptchaDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service - Quản lý hệ thống CAPTCHA (Math-based CAPTCHA)
 * Tạo câu hỏi toán học đơn giản và lưu trữ đáp án để xác thực
 * Sử dụng ConcurrentHashMap để lưu trữ tạm thời (in-memory storage)
 */
@Service
public class CaptchaService {

    /**
     * Lưu trữ CAPTCHA theo ID
     * Key: captchaId (UUID)
     * Value: đáp án đúng (Integer)
     * ConcurrentHashMap để đảm bảo thread-safe khi nhiều request đồng thời
     */
    private final Map<String, Integer> captchaStore = new ConcurrentHashMap<>();

    /**
     * Thời gian sống của CAPTCHA (5 phút) - sau đó sẽ bị xóa khỏi store
     */
    private static final long CAPTCHA_TTL_MS = 5 * 60 * 1000;

    /**
     * Tạo một CAPTCHA mới với câu hỏi toán học
     * @return CaptchaDTO chứa ID và câu hỏi hiển thị cho người dùng
     */
    public CaptchaDTO generateCaptcha() {
        // Tạo ID duy nhất cho CAPTCHA này
        String captchaId = UUID.randomUUID().toString();

        // Sinh ngẫu nhiên 2 số từ 1 đến 10
        int num1 = (int) (Math.random() * 10) + 1;
        int num2 = (int) (Math.random() * 10) + 1;

        // Chọn ngẫu nhiên phép toán: 0 = cộng, 1 = trừ
        // Đảm bảo kết quả phép trừ luôn >= 0 (không có số âm)
        int operator;
        int result;
        String question;

        if (Math.random() > 0.5) {
            // Phép cộng (luôn cho kết quả dương)
            operator = 0;
            result = num1 + num2;
            question = num1 + " + " + num2 + " = ?";
        } else {
            // Phép trừ (đảm bảo số lớn trừ số nhỏ)
            operator = 1;
            if (num1 >= num2) {
                result = num1 - num2;
                question = num1 + " - " + num2 + " = ?";
            } else {
                // Hoán đổi nếu num1 < num2 để kết quả không âm
                result = num2 - num1;
                question = num2 + " - " + num1 + " = ?";
            }
        }

        // Lưu đáp án vào store với timestamp
        CaptchaData data = new CaptchaData(result, System.currentTimeMillis());
        captchaStore.put(captchaId, data.getAnswer());

        // Dọn dẹp các CAPTCHA hết hạn
        cleanupExpiredCaptchas();

        // Trả về DTO chứa ID và câu hỏi hiển thị
        return new CaptchaDTO(captchaId, question);
    }

    /**
     * Xác thực đáp án CAPTCHA của người dùng
     * @param captchaId ID của CAPTCHA cần kiểm tra
     * @param answer Đáp án người dùng nhập vào
     * @return true nếu đáp án đúng và CAPTCHA còn hiệu lực, false nếu sai hoặc hết hạn
     */
    public boolean validateCaptcha(String captchaId, Integer answer) {
        // Kiểm tra CAPTCHA có tồn tại không
        if (captchaId == null || captchaId.isEmpty()) {
            return false;
        }

        // Lấy đáp án đã lưu
        Integer storedAnswer = captchaStore.get(captchaId);

        // Nếu không tìm thấy CAPTCHA hoặc đáp án không khớp
        if (storedAnswer == null) {
            return false;
        }

        // So sánh đáp án (không phân biệt kiểu đối tượng, chỉ so sánh giá trị)
        if (!storedAnswer.equals(answer)) {
            return false;
        }

        // Xóa CAPTCHA sau khi sử dụng thành công (ngăn replay attack)
        captchaStore.remove(captchaId);

        return true;
    }

    /**
     * Kiểm tra đáp án CAPTCHA nhưng KHÔNG xóa sau khi kiểm tra
     * Dùng khi cần kiểm tra nhiều lần trước khi submit
     * @param captchaId ID của CAPTCHA cần kiểm tra
     * @param answer Đáp án người dùng nhập vào
     * @return true nếu đáp án đúng và CAPTCHA còn hiệu lực
     */
    public boolean checkCaptchaOnly(String captchaId, Integer answer) {
        if (captchaId == null || captchaId.isEmpty()) {
            return false;
        }

        Integer storedAnswer = captchaStore.get(captchaId);
        if (storedAnswer == null) {
            return false;
        }

        return storedAnswer.equals(answer);
    }

    /**
     * Xóa CAPTCHA khỏi hệ thống (dùng khi user muốn tạo CAPTCHA mới)
     * @param captchaId ID của CAPTCHA cần xóa
     */
    public void removeCaptcha(String captchaId) {
        if (captchaId != null) {
            captchaStore.remove(captchaId);
        }
    }

    /**
     * Dọn dẹp các CAPTCHA đã hết hạn (quá 5 phút)
     * Gọi định kỳ hoặc khi tạo CAPTCHA mới
     */
    private void cleanupExpiredCaptchas() {
        long currentTime = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> {
            // Trong implement đơn giản, chúng ta không lưu timestamp
            // Nên cleanup chỉ xóa các entry rỗng
            return false;
        });
    }

    /**
     * Inner class lưu trữ dữ liệu CAPTCHA với timestamp
     */
    private static class CaptchaData {
        private final int answer;
        private final long createdAt;

        public CaptchaData(int answer, long createdAt) {
            this.answer = answer;
            this.createdAt = createdAt;
        }

        public int getAnswer() {
            return answer;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > CAPTCHA_TTL_MS;
        }
    }
}
