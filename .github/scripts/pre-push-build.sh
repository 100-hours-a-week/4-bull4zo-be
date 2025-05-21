#!/bin/sh

RED='\033[0;31m'
GREEN='\033[0;32m'
BOLD='\033[1m'
NC='\033[0m'

BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "${BOLD}[pre-push] 현재 브랜치: ${BRANCH}${NC}"

if [ "$BRANCH" = "develop" ]; then
  echo "${BOLD}[pre-push] develop 브랜치 빌드 검사 시작 (spring.profiles.active=dev)...${NC}"

  ./gradlew clean build -x test -Dspring.profiles.active=dev

  if [ $? -ne 0 ]; then
    echo "${RED}[pre-push] 빌드 실패! 푸시가 중단되었습니다.${NC}"
    exit 1
  fi

  echo "${GREEN}[pre-push] 빌드 성공! 푸시 진행됩니다.${NC}"
else
  echo "${GREEN}[pre-push] ${BRANCH} 브랜치는 빌드 체크 없이 푸시 허용.${NC}"
fi

exit 0
