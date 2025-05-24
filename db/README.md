# Test Data SQL Scripts

이 디렉토리는 **성능 테스트** 및 **기능 검증**을 위한 더미 데이터를 삽입하는 SQL 스크립트를 포함하고 있습니다.

## 사용 방법

1. 사전 조건
   1. `user`, `vote`, `vote_response` 테이블이 생성되어 있어야 한다.
   2. `init-data.sql`을 먼저 실행해, 기본 시스템 데이터가 생성되어 있어야 한다.
2. MySQL 접속 후 `moa` 데이터베이스 선택
3. 아래 순서로 실행
   ```bash
    source scripts/db/testdata/truncate_all.sql;
    source scripts/db/testdata/init_users.sql;
    source scripts/db/testdata/init_votes.sql;
    source scripts/db/testdata/init_vote_responses.sql;

## 주의 사항
> 이 스크립트는 테스트 용도입니다. 프로덕션 환경에서는 절대 실행하지 마세요.
