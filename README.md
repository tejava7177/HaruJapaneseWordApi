# HaruJapaneseWordApi

## Run

./gradlew bootRun

## Test API

GET http://localhost:8080/health

## GitHub Actions Deploy

- Workflow file: `.github/workflows/deploy.yml`
- Triggers: push to `main`, manual `workflow_dispatch`
- Required GitHub secrets: `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`
- Deployment flow: GitHub Actions builds the app, SSHes into the Ubuntu EC2 host, updates `~/HaruJapaneseWordApi`, rebuilds with `./gradlew clean bootJar`, and restarts the app with `docker-compose`
