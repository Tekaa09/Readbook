-- Redbook: đăng nhập an toàn cho bản admin (database MySQL trên máy của admin).
-- Bản người dùng chạy offline, không kết nối database. Chạy bằng root; chạy lại nhiều lần vẫn được.
USE redbook;

-- 1) Kiểm tra đăng nhập ngay trên máy chủ. Mật khẩu lưu dạng băm: sha2$<salt>$<SHA-256(salt + mật khẩu)>.
--    Tài khoản thêm bằng Workbench với mật khẩu chữ thường vẫn đăng nhập được,
--    và được đổi sang dạng băm ở lần đăng nhập đầu tiên.
DROP PROCEDURE IF EXISTS login_user;
DELIMITER $$
CREATE PROCEDURE login_user(IN p_email VARCHAR(100), IN p_password VARCHAR(100))
    SQL SECURITY DEFINER
BEGIN
    DECLARE v_id INT DEFAULT NULL;
    DECLARE v_stored VARCHAR(100);
    DECLARE v_salt VARCHAR(32);

    SELECT user_id, password INTO v_id, v_stored
      FROM users WHERE email = TRIM(p_email) LIMIT 1;

    IF v_id IS NOT NULL THEN
        IF v_stored LIKE 'sha2$%$%' THEN
            SET v_salt = SUBSTRING_INDEX(SUBSTRING_INDEX(v_stored, '$', 2), '$', -1);
            IF v_stored <> CONCAT('sha2$', v_salt, '$', SHA2(CONCAT(v_salt, p_password), 256)) THEN
                SET v_id = NULL;
            END IF;
        ELSEIF CAST(v_stored AS BINARY) = CAST(p_password AS BINARY) THEN
            SET v_salt = LEFT(SHA2(CONCAT(RAND(), NOW(6), v_id), 256), 16);
            UPDATE users SET password = CONCAT('sha2$', v_salt, '$', SHA2(CONCAT(v_salt, p_password), 256))
             WHERE user_id = v_id;
        ELSE
            SET v_id = NULL;
        END IF;
    END IF;

    SELECT user_id, email, role FROM users WHERE user_id = v_id;
END$$
DELIMITER ;

-- 2) Băm luôn các mật khẩu đang để dạng chữ thường.
UPDATE users
   SET password = CONCAT('sha2$', LEFT(SHA2(CONCAT('redbook', user_id, email), 256), 16), '$',
                         SHA2(CONCAT(LEFT(SHA2(CONCAT('redbook', user_id, email), 256), 16), password), 256))
 WHERE password NOT LIKE 'sha2$%$%';

-- 3) Sổ từ vựng: từ trong đề (dạng chọn từ) kèm nghĩa, admin sửa bằng Workbench.
--    Bản người dùng nhận bảng này qua file dữ liệu đóng gói (build.bat).
CREATE TABLE IF NOT EXISTS vocab (
    vocab_id INT NOT NULL AUTO_INCREMENT,
    word VARCHAR(60) NOT NULL,
    part_of_speech VARCHAR(20) DEFAULT NULL,
    meaning_en VARCHAR(255) DEFAULT NULL,
    meaning_vi VARCHAR(255) DEFAULT NULL,
    example TEXT,
    question_id INT DEFAULT NULL,
    PRIMARY KEY (vocab_id),
    UNIQUE KEY word_unique (word),
    KEY vocab_question_idx (question_id),
    CONSTRAINT vocab_question FOREIGN KEY (question_id) REFERENCES questions (question_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
