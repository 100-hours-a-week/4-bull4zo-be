-- 시스템 유저
INSERT INTO `user` (id, nickname, email, role, user_status, last_active_at, created_at, updated_at)
SELECT 1, 'SYSTEM', 'system@moa.com', 'ADMIN', 'ACTIVE', NOW(), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE id = 1
);

-- 공개 그룹
INSERT INTO `group` (id, user_id, name, description, invite_code, created_at, updated_at)
SELECT 1, 1, '공개', '모든 사용자가 속한 공개 투표 그룹입니다.', 'public', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `group` WHERE id = 1
);