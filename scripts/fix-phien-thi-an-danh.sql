-- Sửa lỗi: Column 'IDnguoi_dung' cannot be null (thi ẩn danh qua link)
-- Chạy trên database webthitracnghiem (phpMyAdmin > SQL, hoặc: mysql -u root webthitracnghiem < fix-phien-thi-an-danh.sql)

ALTER TABLE phien_thi MODIFY COLUMN IDnguoi_dung VARCHAR(36) NULL;

-- Nếu bảng chưa có cột ho_ten_an_danh (bỏ comment dòng dưới nếu báo lỗi "Duplicate column"):
-- ALTER TABLE phien_thi ADD COLUMN ho_ten_an_danh VARCHAR(200) NULL;
