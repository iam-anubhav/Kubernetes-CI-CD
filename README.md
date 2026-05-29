# BankApp - Challenge Ready Kubernetes Project

This project is a minimal production-style Spring Boot app designed for the DevOps Engineer Kubernetes + CI/CD challenge.

## What is included

- Spring Boot 3.3.3 backend
- Thymeleaf frontend
- MySQL database dependency
- Dockerfile
- Docker Compose for local run
- Kubernetes manifests for AKS
- Azure DevOps pipeline
- Trivy filesystem scan
- Trivy image scan
- Readiness/liveness probes
- Persistent storage for MySQL
- Resource requests/limits
- Basic auth with DB-backed users

## What you will see on localhost

Open:

```bash
http://localhost:8080
```

You will be redirected to the login page. After login, you get:
- dashboard
- deposit
- withdraw
- transfer
- transaction history

## Local run

### With Docker Compose
```bash
docker compose up --build
```

App:
- `http://localhost:8080/`

### Register a user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Password123","initialBalance":100.00}'
```

### Basic auth request
```bash
curl -u alice:Password123 http://localhost:8080/api/accounts/me
```

## Kubernetes setup

### 1. Create the namespace and secrets
```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-secret.yaml
```

### 2. Apply MySQL storage and service
```bash
kubectl apply -f k8s/02-mysql-pvc.yaml
kubectl apply -f k8s/04-mysql-service.yaml
kubectl apply -f k8s/03-mysql-statefulset.yaml
```

### 3. Apply the application
```bash
kubectl apply -f k8s/05-app-deployment.yaml
kubectl apply -f k8s/06-app-service.yaml
```

### 4. Check rollout
```bash
kubectl get pods -n bankapp
kubectl get svc -n bankapp
kubectl rollout status deploy/bankapp -n bankapp
```

## Azure DevOps pipeline

Before using `azure-pipelines.yml`, create:
- Docker Registry service connection named `Docker-ACR-connection`
- Kubernetes service connection named `AKS-Connection`

Then replace:
- `placeholder.azurecr.io`
with your real ACR login server.

Also replace:
- agent pool name
- agent demand
if your self-hosted agent name is different.

## Strong recommendation for your demo

Use this failure scenario live:

### Option A: bad DB password
1. Change `DB_PASSWORD` in `k8s/01-secret.yaml` to a wrong value.
2. Apply it.
3. Watch the app fail readiness.
4. Use:
```bash
kubectl get pods -n bankapp
kubectl logs deploy/bankapp -n bankapp
kubectl describe pod <pod-name> -n bankapp
```
5. Fix the secret and reapply.

### Option B: broken image tag
1. Change the image tag in the deployment to something invalid.
2. Apply it.
3. Show `ImagePullBackOff`.
4. Fix the tag and redeploy.

### Option C: broken readiness probe
1. Change the readiness path temporarily.
2. Show the pod stays unready.
3. Fix the probe and watch traffic recover.

## What to say in the interview

- Why AKS: supported by the challenge and keeps Kubernetes visible.
- Why probes: they prevent broken pods from receiving traffic.
- Why PVC: MySQL must keep data across restarts.
- Why Trivy: catches insecure dependencies and images early.
- Why immutable image tags: every build is traceable and reproducible.
- Why this failure demo: it proves you can debug, not just deploy.

## Files to customize

- `azure-pipelines.yml`
- `k8s/01-secret.yaml`
- `k8s/05-app-deployment.yaml`
- `k8s/06-app-service.yaml`
- `k8s/03-mysql-statefulset.yaml`
