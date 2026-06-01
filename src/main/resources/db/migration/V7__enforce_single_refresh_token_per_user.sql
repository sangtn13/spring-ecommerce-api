DELETE rt1
FROM refresh_token rt1
JOIN refresh_token rt2
  ON rt1.user_id = rt2.user_id
 AND (
      rt1.created_at < rt2.created_at
      OR (rt1.created_at = rt2.created_at AND rt1.id < rt2.id)
 );

ALTER TABLE refresh_token
    ADD UNIQUE KEY uk_refresh_token_user_id (user_id);
