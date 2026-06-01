INSERT INTO role (name, created_at, updated_at, created_by, updated_by)
SELECT 'Manager', NOW(), NOW(), 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE name = 'Manager'
);
