-- Chạy thủ công trên MySQL nếu bảng de_thi đã tồn tại mà chưa có cột (vd. môi trường không dùng spring.jpa.hibernate.ddl-auto=update).
-- Mặc định 1 = cho phép xem lại (tương thích dữ liệu cũ).

ALTER TABLE de_thi
    ADD COLUMN cho_phep_xem_lai TINYINT(1) NOT NULL DEFAULT 1
    COMMENT 'Sinh viên được xem lại chi tiết từng câu sau khi nộp';
