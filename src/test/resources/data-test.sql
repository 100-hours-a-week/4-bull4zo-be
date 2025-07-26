-- src/test/resources/data-test.sql
INSERT INTO "user" (id, nickname, role, user_status, last_active_at, created_at, updated_at) VALUES (100, 'SYSTEM_AI_USER', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO "group" (id, name, user_id, invite_code, description, created_at, updated_at) VALUES (1, '공개 그룹', 100, 'ABC123', '테스트 그룹입니다.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
