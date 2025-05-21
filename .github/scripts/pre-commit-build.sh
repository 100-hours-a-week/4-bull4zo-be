#!/bin/sh

RED='\033[0;31m'
GREEN='\033[0;32m'
BOLD='\033[1m'
NC='\033[0m'

echo "${BOLD}[pre-commit] spotlessCheck 실행 중...${NC}"
./gradlew spotlessCheck

# 변경 사항이 생겼으면 커밋 중단
if [ $? -ne 0 ]; then
  echo "${RED}[pre-commit] 코드 스타일 위반이 있습니다. spotlessApply로 먼저 포맷해주세요.${NC}"
  exit 1
else
  echo "${GREEN}[pre-commit] 코드 스타일 검사 통과!${NC}"
fi
