USE moa;

DELIMITER $$

DROP PROCEDURE IF EXISTS insert_vote_responses$$

CREATE PROCEDURE insert_vote_responses()
BEGIN
  DECLARE vote_id INT DEFAULT 1;
  DECLARE user_id INT;
  DECLARE vote_offset INT;

  WHILE vote_id <= 100 DO
      SET user_id = 2;
      SET vote_offset = vote_id; -- vote_id에 따른 시간 보정용

      WHILE user_id <= 201 DO
          INSERT INTO vote_response (
            vote_id,
            user_id,
            option_number,
            voted_at
          )
          VALUES (
                   vote_id,
                   user_id,
                   IF(RAND() < 0.5, 1, 2),
                   IF(vote_id <= 50,
                      DATE_SUB(NOW(), INTERVAL vote_offset HOUR), -- 과거 시간 (종료된 투표)
                      DATE_SUB(NOW(), INTERVAL (100 - vote_offset) MINUTE) -- 진행 중 투표
                   )
                 );
          SET user_id = user_id + 1;
        END WHILE;

      SET vote_id = vote_id + 1;
    END WHILE;
END$$

DELIMITER ;

CALL insert_vote_responses();
DROP PROCEDURE insert_vote_responses;

-- 다음 삽입을 위한 AUTO_INCREMENT 초기화
ALTER TABLE vote_response AUTO_INCREMENT = 20001;
