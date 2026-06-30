# EC2 + ECR + GitHub Actions backend deploy

This deploy path builds the Spring Boot backend into a Docker image, pushes it to Amazon ECR, then updates Docker Compose on one EC2 host. The app connects to RDS for MySQL and to ElastiCache Redis by default.

For the Korean operator checklist, see `docs/deploy-operator-checklist.ko.md`.

## AWS resources

1. Create an ECR private repository, for example `mohae-backend`.
2. Create an IAM role for GitHub Actions OIDC with permission to push to that repository.
3. Give the EC2 instance permission to pull from ECR, either with an instance profile or by letting the workflow pass a short-lived ECR login password during deploy.
4. Create an RDS MySQL database and allow inbound traffic from the EC2 security group on port `3306`.
5. Create an ElastiCache Redis cluster and allow inbound traffic from the EC2 security group on port `6379`.
6. Open the EC2 security group for the public HTTP port you use, usually `8080` or `80` behind a reverse proxy.

## EC2 setup

Install Docker, the Docker Compose plugin, and curl on the EC2 instance.

Create the deploy directory:

```bash
sudo mkdir -p /opt/mohae/backend
sudo chown "$USER":"$USER" /opt/mohae/backend
```

Create `src/main/resources/secret/prod.env` in the secret submodule from `deploy/.env.production.example` and replace every secret value. Set `DB_URL` and `BATCH_DB_URL` to the RDS endpoint, and set `REDIS_HOST` to the ElastiCache primary endpoint.

The deploy workflow uploads `src/main/resources/secret/prod.env` to `/opt/mohae/backend/.env` on EC2 on every deploy.

The default compose file runs only the backend container. It includes an optional Redis service for early internal testing:

```bash
cd /opt/mohae/backend
REDIS_HOST=redis docker compose --env-file .env -f docker-compose.yml --profile internal-redis up -d
```

## GitHub secrets

Set these repository secrets:

```text
AWS_REGION=ap-northeast-2
AWS_ROLE_TO_ASSUME=arn:aws:iam::<account-id>:role/<github-actions-deploy-role>
ECR_REPOSITORY=mohae-backend
EC2_HOST=<ec2-public-ip-or-domain>
EC2_USER=ubuntu
EC2_SSH_PRIVATE_KEY=<private-key-that-can-ssh-to-ec2>
EC2_DEPLOY_DIR=/opt/mohae/backend
SUBMODULE_TOKEN=<token-that-can-read-the-secret-submodule>
```

`EC2_DEPLOY_DIR` is optional. The workflow defaults to `/opt/mohae/backend`.
`SUBMODULE_TOKEN` is needed when the secret submodule is private and the default `GITHUB_TOKEN` cannot read it.

## Deploy

Push to `main` or run the `Deploy Backend` workflow manually.

The workflow:

1. builds the Docker image from `Dockerfile`;
2. pushes both `<sha>` and `latest` tags to ECR;
3. uploads `deploy/docker-compose.prod.yml` to EC2;
4. runs `docker compose pull app && docker compose up -d`;
5. checks `http://127.0.0.1:${APP_PORT:-8080}/actuator/health`.

## Useful EC2 commands

```bash
cd /opt/mohae/backend
docker compose --env-file .env -f docker-compose.yml ps
docker compose --env-file .env -f docker-compose.yml logs -f app
docker compose --env-file .env -f docker-compose.yml restart app
```
