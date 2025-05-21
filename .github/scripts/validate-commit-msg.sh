#!/bin/sh

RED='\033[0;31m'
GREEN='\033[0;32m'
BOLD='\033[1m'
NC='\033[0m'

REGEX="^(feat|fix|docs|style|refactor|test|chore|cicd|release|rename|remove|build): .+"

COMMIT_MSG_FILE=$1
COMMIT_MSG=$(head -n1 "$COMMIT_MSG_FILE")

echo "${BOLD}[commit-msg] 커밋 메시지 검사 중...${NC}"

if echo "$COMMIT_MSG" | grep -Eq "$REGEX"; then
  echo "${GREEN}[commit-msg] 형식 통과: ${COMMIT_MSG}${NC}"
  exit 0
else
  echo "${RED}[commit-msg] 커밋 메시지 형식 오류${NC}"
  echo "${RED}형식: <타입>: 메시지 (#이슈번호)${NC}"
  echo "${RED}예시: feat: 투표 등록 기능 구현 (#32)${NC}"
  exit 1
fi
