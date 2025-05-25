USE moa;

INSERT INTO vote_result (vote_id, option_number, count, ratio, created_at, updated_at)
SELECT
  vr.vote_id,
  vr.option_number,
  vr.count,
  IFNULL(vr.count / total.total_count, 0.0) AS ratio,
  NOW(),
  NOW()
FROM
  (
    SELECT
      vote_id,
      option_number,
      COUNT(*) AS count
    FROM vote_response
    GROUP BY vote_id, option_number
  ) vr
    JOIN
  (
    SELECT
      vote_id,
      COUNT(*) AS total_count
    FROM vote_response
    WHERE option_number IN (1, 2)
    GROUP BY vote_id
  ) total ON vr.vote_id = total.vote_id;
