#!/bin/sh

# sh .github/scripts/install-hooks.sh 명령어로 실행하시면 됩니다. 
cp .github/scripts/pre-push-build.sh .git/hooks/pre-push
chmod +x .git/hooks/pre-push
echo "pre-push hook 설치 완료!"