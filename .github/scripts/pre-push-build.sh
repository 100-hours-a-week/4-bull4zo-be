#!/bin/sh

BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "현재 브랜치: $BRANCH"

if [ "$BRANCH" = "develop" ]; then
  echo "develop 브랜치 빌드 검사 시작 (spring.profiles.active=dev)..."

  ./gradlew clean build -x test -Dspring.profiles.active=dev
  if [ $? -ne 0 ]; then
    echo "빌드 실패! push 차단"
    exit 1
  fi

  echo "빌드 성공! push 진행."
else
  echo "$BRANCH 브랜치는 빌드 체크 없이 push 허용"
fi

exit 0