USE moa;

DELETE FROM vote_response;
DELETE FROM vote;
DELETE FROM `user` WHERE id != 1;
TRUNCATE TABLE vote_result;
