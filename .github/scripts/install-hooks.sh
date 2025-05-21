#!/bin/sh

# sh .github/scripts/install-hooks.sh 명령어로 실행하시면 됩니다.

# pre-push hook 설치
cp .github/scripts/pre-push-build.sh .git/hooks/pre-push
chmod +x .git/hooks/pre-push
echo "[✓] pre-push hook 설치 완료!"

# pre-commit hook 설치
cp .github/scripts/pre-commit-build.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
echo "[✓] pre-commit hook 설치 완료!"
