USE moa;

DELIMITER $$

DROP PROCEDURE IF EXISTS insert_users$$

CREATE PROCEDURE insert_users()
BEGIN
  DECLARE i INT DEFAULT 2;

  WHILE i <= 201 DO
      INSERT INTO `user` (
        id,
        nickname,
        email,
        role,
        user_status,
        last_active_at,
        created_at,
        updated_at,
        withdrawn_at
      )
      VALUES (
               i,
               CONCAT('testuser', LPAD(i, 4, '0')),
               CONCAT('testuser', LPAD(i, 4, '0'), '@example.com'),
               'USER',
               'ACTIVE',
               NOW(),
               NOW(),
               NOW(),
               NULL
             );
      SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

CALL insert_users();
DROP PROCEDURE insert_users;

-- 다음 삽입을 위한 AUTO_INCREMENT 초기화
ALTER TABLE `user` AUTO_INCREMENT = 202;
