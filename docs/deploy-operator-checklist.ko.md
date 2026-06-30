# 백엔드 배포 준비 체크리스트

이 문서는 배포 자동화 코드가 아니라, 사람이 AWS/GitHub/EC2에서 직접 해야 하는 작업만 정리한 문서입니다.

배포 구조는 다음과 같습니다.

1. GitHub Actions가 백엔드 Docker 이미지를 빌드합니다.
2. 이미지를 Amazon ECR에 push합니다.
3. EC2에 접속해서 `docker-compose.yml`을 갱신합니다.
4. EC2에서 ECR 이미지를 pull하고 백엔드 컨테이너를 재시작합니다.
5. 백엔드는 RDS MySQL과 ElastiCache Redis에 연결합니다.

## 1. AWS 리소스 만들기

### ECR

ECR private repository를 만듭니다.

추천 이름:

```text
mohae-backend
```

GitHub Actions secret의 `ECR_REPOSITORY` 값도 이 이름과 같아야 합니다.

### RDS MySQL

RDS MySQL을 생성합니다.

필요한 값:

```text
RDS endpoint
DB name: mohae
Batch DB name: mohae_backend_batch
DB user
DB password
```

보안 그룹 설정:

```text
RDS inbound 3306
source: 백엔드 EC2 보안 그룹
```

### ElastiCache Redis

ElastiCache Redis를 생성합니다.

필요한 값:

```text
Primary endpoint
Port: 6379
```

보안 그룹 설정:

```text
ElastiCache inbound 6379
source: 백엔드 EC2 보안 그룹
```

초기에는 EC2 내부 Redis를 쓸 수도 있습니다. 그 경우 `.env`에서 `REDIS_HOST=redis`로 두고, compose 실행 시 `--profile internal-redis`를 사용합니다.

### EC2

백엔드 컨테이너를 실행할 EC2를 준비합니다.

보안 그룹 설정:

```text
HTTP 포트 inbound: 8080 또는 리버스 프록시를 쓰면 80/443
SSH inbound: 22, 본인 IP만 허용 권장
Outbound: RDS 3306, ElastiCache 6379, ECR 접근 가능
```

EC2에 Docker, Docker Compose plugin, curl을 설치합니다.

Ubuntu 예시:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo tee /etc/apt/keyrings/docker.asc > /dev/null
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker "$USER"
```

설치 후 SSH를 다시 접속하고 확인합니다.

```bash
docker --version
docker compose version
curl --version
```

## 2. GitHub Actions용 IAM 설정

GitHub Actions가 ECR에 이미지를 push할 수 있어야 합니다.

권장 방식은 GitHub OIDC용 IAM Role을 만드는 것입니다.

GitHub Actions secret에 들어갈 값:

```text
AWS_ROLE_TO_ASSUME=arn:aws:iam::<account-id>:role/<github-actions-deploy-role>
AWS_REGION=ap-northeast-2
```

이 Role에는 최소한 다음 권한이 필요합니다.

```text
ecr:GetAuthorizationToken
ecr:BatchCheckLayerAvailability
ecr:InitiateLayerUpload
ecr:UploadLayerPart
ecr:CompleteLayerUpload
ecr:PutImage
ecr:BatchGetImage
```

대상 repository는 가능하면 `mohae-backend` ECR repository로 제한합니다.

## 3. EC2 배포 디렉터리 만들기

EC2에 SSH로 접속해서 배포 디렉터리를 만듭니다.

```bash
sudo mkdir -p /opt/mohae/backend
sudo chown "$USER":"$USER" /opt/mohae/backend
cd /opt/mohae/backend
```

## 4. secret 서브모듈에 `prod.env` 만들기

로컬의 `deploy/.env.production.example`을 기준으로 secret 서브모듈에 `src/main/resources/secret/prod.env`를 만듭니다.

배포 workflow는 매번 이 파일을 EC2의 `/opt/mohae/backend/.env`로 업로드합니다. EC2에 직접 `.env`를 만들 필요는 없습니다.

필수로 바꿀 값:

```text
DB_URL=jdbc:mysql://<rds-endpoint>:3306/mohae?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USER_NAME=<rds-user>
DB_PASSWORD=<rds-password>
BATCH_DB_URL=jdbc:mysql://<rds-endpoint>:3306/mohae_backend_batch?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
BATCH_DB_USER_NAME=<rds-user>
BATCH_DB_PASSWORD=<rds-password>
REDIS_HOST=<elasticache-primary-endpoint>
JWT_SECRET=<긴 랜덤 문자열>
GOOGLE_CLIENT_ID_WEB=<구글 웹 클라이언트 ID>
GOOGLE_CLIENT_ID_ANDROID=<구글 안드로이드 클라이언트 ID>
GOOGLE_CLIENT_ID_IOS=<구글 iOS 클라이언트 ID>
WEB_PUSH_SUBJECT=mailto:<관리자 이메일>
WEB_PUSH_PUBLIC_KEY=<VAPID public key>
WEB_PUSH_PRIVATE_KEY=<VAPID private key>
CHAT_LOCAL_PUBLIC_BASE_URL=<백엔드 공개 주소>/uploads
```

운영 초기에 DB schema 자동 생성/수정을 허용하려면:

```text
SPRING_SQL_INIT_MODE=always
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

운영 안정화 후에는 `SPRING_SQL_INIT_MODE=never`, `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` 또는 `none`으로 낮추는 것을 권장합니다.

## 5. GitHub repository secrets 등록

GitHub repository의 `Settings > Secrets and variables > Actions`에서 아래 secrets를 등록합니다.

```text
AWS_REGION=ap-northeast-2
AWS_ROLE_TO_ASSUME=arn:aws:iam::<account-id>:role/<github-actions-deploy-role>
ECR_REPOSITORY=mohae-backend
EC2_HOST=<ec2-public-ip-or-domain>
EC2_USER=ubuntu
EC2_SSH_PRIVATE_KEY=<EC2 접속용 private key 내용 전체>
EC2_DEPLOY_DIR=/opt/mohae/backend
SUBMODULE_TOKEN=<secret 서브모듈을 읽을 수 있는 토큰>
```

`EC2_DEPLOY_DIR`는 생략할 수 있습니다. 생략하면 workflow가 `/opt/mohae/backend`를 사용합니다.

`EC2_SSH_PRIVATE_KEY`는 `-----BEGIN ...`부터 `-----END ...`까지 줄바꿈을 포함해서 넣습니다.

`SUBMODULE_TOKEN`은 secret 서브모듈이 private이고 기본 `GITHUB_TOKEN`으로 checkout이 안 될 때 필요합니다. classic PAT를 쓴다면 private repo 읽기 권한만 최소로 줍니다.

## 6. 첫 배포 실행

GitHub Actions의 `Deploy Backend` workflow를 수동 실행하거나 `main` 브랜치에 push합니다.

첫 배포에서 workflow가 하는 일:

1. Docker image build
2. ECR push
3. EC2에 `docker-compose.yml` 업로드
4. EC2에서 `docker compose pull app`
5. EC2에서 `docker compose up -d`
6. `/actuator/health` 확인

## 7. 배포 후 확인

EC2에서 상태를 확인합니다.

```bash
cd /opt/mohae/backend
docker compose --env-file .env -f docker-compose.yml ps
docker compose --env-file .env -f docker-compose.yml logs -f app
curl -fsS http://127.0.0.1:${APP_PORT:-8080}/actuator/health
```

외부에서 접근 확인:

```bash
curl -fsS http://<ec2-public-ip-or-domain>:8080/actuator/health
```

## 8. 내부 Redis를 임시로 쓰는 경우

ElastiCache 대신 EC2 내부 Redis를 임시로 쓰려면 EC2의 `.env`를 이렇게 둡니다.

```text
REDIS_HOST=redis
REDIS_PORT=6379
```

그리고 compose를 profile 포함해서 실행합니다.

```bash
cd /opt/mohae/backend
docker compose --env-file .env -f docker-compose.yml --profile internal-redis up -d
```

GitHub Actions workflow는 기본적으로 app만 `pull`합니다. 내부 Redis는 EC2에서 한 번 profile로 띄워두면 됩니다.

## 9. 자주 볼 명령어

```bash
cd /opt/mohae/backend
docker compose --env-file .env -f docker-compose.yml ps
docker compose --env-file .env -f docker-compose.yml logs --tail 200 app
docker compose --env-file .env -f docker-compose.yml restart app
docker image prune -f
```

## 10. 문제 생기면 먼저 볼 것

GitHub Actions 실패:

```text
AWS_ROLE_TO_ASSUME 값이 맞는지
OIDC trust policy에 repository/branch 조건이 맞는지
ECR_REPOSITORY 이름이 실제 ECR repository와 같은지
EC2_HOST, EC2_USER, EC2_SSH_PRIVATE_KEY로 SSH 접속이 되는지
```

앱 컨테이너 실행 실패:

```text
secret 서브모듈에 src/main/resources/secret/prod.env가 존재하는지
EC2의 /opt/mohae/backend/.env로 prod.env가 업로드됐는지
DB_URL이 RDS endpoint를 보고 있는지
RDS 보안 그룹이 EC2 보안 그룹을 허용하는지
REDIS_HOST가 ElastiCache endpoint 또는 internal redis service 이름인지
JWT_SECRET, GOOGLE_CLIENT_ID_*, WEB_PUSH_* 값이 비어 있지 않은지
```

헬스 체크 실패:

```bash
cd /opt/mohae/backend
docker compose --env-file .env -f docker-compose.yml logs --tail 200 app
docker compose --env-file .env -f docker-compose.yml ps
```
