-- Shift legacy timestamps that were stored in UTC to Asia/Ho_Chi_Minh (+07:00).
-- This migration is executed once by Flyway and preserves existing data.

UPDATE role
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

UPDATE `user`
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

UPDATE category
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

UPDATE product
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

UPDATE image
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

UPDATE cart
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

UPDATE cart_item
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

UPDATE orders
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

UPDATE order_item
SET created_at = DATE_ADD(created_at, INTERVAL 7 HOUR),
    updated_at = DATE_ADD(updated_at, INTERVAL 7 HOUR)
WHERE created_at IS NOT NULL
  AND updated_at IS NOT NULL;

-- payment is intentionally excluded because existing values are already in local time.
