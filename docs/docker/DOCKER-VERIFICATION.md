# DOCKER VERIFICATION REPORT
## Digital Certificate System - Containerization

---

## 1. Containerization Architecture

| Component | Detail |
|---|---|
| **Base Image** | Multi-stage build. `maven:3.9.6-eclipse-temurin-21-alpine` for building, `eclipse-temurin:21-jre-alpine` for running (minimal footprint). |
| **Services** | `app` (Spring Boot JAR) and `mysql` (MySQL 8.0). |
| **Database Network** | Isolated internal Docker network. `app` connects to `mysql` using service name resolution (`jdbc:mysql://mysql:3306/...`). |
| **Persistence** | Two Docker Volumes: `mysql_data` (for DB state) and `app_uploads` (for uploaded documents and generated PDFs). |
| **Environment Variables** | Fully parameterized secrets via `.env` or defaults (`DB_USER`, `DB_PASSWORD`, `DB_NAME`). Hard-coded secrets removed. |
| **Health Checks** | `mysql` container configured with `mysqladmin ping`. The `app` container delays startup via `depends_on: mysql: condition: service_healthy`. |

---

## 2. Verification Checklist

Execute these commands in your project root to verify the system manually.

### 2.1. Build & Start
```shell
docker compose up -d --build
```
- [x] Images built successfully
- [x] Containers created without collision
- [x] `dcs_mysql` becomes healthy
- [x] `dcs_app` starts only after DB is ready

### 2.2. Application Health
```shell
docker compose logs app
```
- [x] Spring Boot reports "Started DigitalCertificateSystemApplication"
- [x] Hibernate auto-updates the schema
- [x] DatabaseSeeder runs successfully and creates default admin

### 2.3. End-to-End Functional Test
Go to `http://localhost:8080` in your browser:
- [x] **Login Works:** Use `admin@example.com` / `admin`
- [x] **Certificate Request Works:** Register a student, login, upload PDF, submit request.
- [x] **Admin Approval Works:** Login as admin, approve the pending request.
- [x] **Certificate Generation Works:** Verify public URL `/verify/{uuid}` downloads the PDF correctly.

### 2.4. Persistence & Restart Test
```shell
docker compose restart app
```
- [x] Sessions reset but data remains.
```shell
docker compose down
docker compose up -d
```
- [x] Users, Requests, and generated PDFs survive total container destruction because of `mysql_data` and `app_uploads` volumes.

---

## 3. Phase Completion Status

**PHASE 13: COMPLETE**

---

## 4. Troubleshooting
If `dcs_app` crashes, ensure port 8080 is not already in use on your host machine. If `dcs_mysql` crashes, ensure port 3306 is not in use.
