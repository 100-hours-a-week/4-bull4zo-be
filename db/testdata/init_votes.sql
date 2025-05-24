USE moa;

DELIMITER $$

DROP PROCEDURE IF EXISTS insert_votes$$

CREATE PROCEDURE insert_votes()
BEGIN
  DECLARE i INT DEFAULT 1;

  WHILE i <= 100 DO
      INSERT INTO vote (
        id,
        user_id,
        group_id,
        content,
        image_url,
        closed_at,
        anonymous,
        vote_status,
        admin_vote,
        vote_type,
        last_anonymous_number,
        created_at,
        updated_at
      )
      VALUES (
               i,
               1,  -- user_id
               1,  -- group_id
               CONCAT('Test Vote ', i),
               NULL,
               IF(i <= 50, NOW() - INTERVAL i HOUR, NOW() + INTERVAL i HOUR),
               false,
               'OPEN',
               false,
               'USER',
               0,
               NOW(),
               NOW()
             );
      SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

CALL insert_votes();
DROP PROCEDURE insert_votes;

-- 다음 삽입을 위한 AUTO_INCREMENT 초기화
ALTER TABLE vote AUTO_INCREMENT = 101;
