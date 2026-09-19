# DIGITAL CERTIFICATE REQUEST AND APPROVAL SYSTEM
## PROJECT CONTRACT AND TECHNICAL BLUEPRINT

### 1. Problem Statement
Many organizations process certificate requests manually via paper forms or emails. This leads to delays, lost requests, lack of transparency, and high susceptibility to fraud (forged certificates). A centralized digital system is needed to streamline requests, approvals, and secure generation of verifiable digital certificates.

### 2. Objectives
- Automate the end-to-end certificate request and approval workflow.
- Provide a secure, authenticated platform for both Users and Administrators.
- Generate tamper-evident digital certificates (PDF with QR code/UUID).
- Provide a public verification mechanism to ensure certificate authenticity.

### 3. Functional Requirements
- **User Management**: Registration, Login, Profile updates.
- **Certificate Requests**: Submit requests, upload supporting documents, track request status.
- **Admin Management**: Review requests, approve/reject with reasons, manage users and certificate types.
- **Generation**: Auto-generate PDF certificates upon approval.
- **Verification**: Publicly accessible URL to verify certificate UUID/QR.
- **Audit**: Log significant system actions (approvals, rejections).

### 4. Non-functional Requirements
- **Security**: Passwords encrypted (Bcrypt), RBAC authorization, secure sessions against CSRF/XSS.
- **Performance**: Page load and API response times < 2s.
- **Scalability**: Stateless backend capable of horizontal scaling.
- **Availability**: Containerized deployment via Docker/Docker Compose for reliable uptime.

### 5. User Roles
- `ROLE_USER`: Standard applicant requesting certificates.
- `ROLE_ADMIN`: Administrator managing and approving requests.

### 6. Complete User Workflow
1. Register -> 2. Login -> 3. View Dashboard -> 4. Select Certificate Type -> 5. Fill Form & Upload Docs -> 6. Submit -> 7. Track Status (PENDING -> APPROVED/REJECTED) -> 8. Download PDF (if APPROVED).

### 7. Complete Admin Workflow
1. Login -> 2. View Dashboard -> 3. View Pending Requests -> 4. Inspect Documents -> 5. Approve or Reject (with reason) -> 6. View Audit Logs.

### 8. Certificate Lifecycle
`PENDING` -> `APPROVED` (Generates Certificate) or `REJECTED`.

### 9. System Architecture
N-Tier Monolith:
- Frontend: HTML/Thymeleaf templates
- Backend: Java 21 / Spring Boot 3.x
- Database: MySQL 8.0
- Infrastructure: Docker -> Jenkins -> Ansible

### 10. Backend Architecture
Controllers (Spring MVC) -> Services (Transactional Logic) -> Repositories (Spring Data JPA) -> MySQL.

### 11. Frontend Architecture
Thymeleaf templates (Server-Side Rendered), Bootstrap 5 for responsive UI, Vanilla JS for dynamic client-side validation.

### 12. Database ER Design
Users (1) --- (N) CertificateRequests
CertificateTypes (1) --- (N) CertificateRequests
CertificateRequests (1) --- (1) Certificates

### 13. Entity Relationship Design (JPA)
- `User`: `id`, `email`, `password`, `role`, `firstName`, `lastName`
- `CertificateType`: `id`, `name`, `description`
- `CertificateRequest`: `id`, `user_id`, `type_id`, `status`, `rejectionReason`, `createdAt`, `updatedAt`, `documentUrl`
- `Certificate`: `id`, `request_id`, `uuid`, `pdfUrl`, `issuedAt`

### 14. API Design
- `POST /register`, `POST /login`
- `GET /user/dashboard`, `GET /user/requests`
- `POST /user/requests/submit`
- `GET /admin/dashboard`, `GET /admin/requests`
- `POST /admin/requests/{id}/approve`, `POST /admin/requests/{id}/reject`
- `GET /verify/{uuid}`

### 15. Security Architecture
Spring Security with Form Login (Session-based). CSRF enabled. Password encoding with BCrypt. Method-level security (`@PreAuthorize`).

### 16. Testing Architecture
- Unit: JUnit 5, Mockito.
- Integration: Spring Boot Test, MockMvc, Testcontainers (optional) or H2.
- E2E: Selenium (optional) or just comprehensive MockMvc.

### 17. Docker Architecture
- `Dockerfile`: Multi-stage build (Maven build -> JRE runtime).
- `docker-compose.yml`: App container + MySQL container + volumes for persistent storage.

### 18. Jenkins Architecture
Jenkinsfile with stages: Checkout, Build, Test, Docker Build, Deploy.

### 19. Ansible Architecture
Playbook to provision server, install Docker, copy compose file, and run containers.

### 20. Complete CI/CD Architecture
Dev pushes to GitHub -> Webhook triggers Jenkins -> Jenkins builds/tests -> Jenkins builds Docker image -> Ansible deploys to target environment.

### 21. Folder Structure
Standard Maven directory structure `src/main/java/com/system/certificate/...`

### 22. Naming Conventions
- Classes/Interfaces: PascalCase (e.g., `CertificateService`).
- Methods/Variables: camelCase (e.g., `getCertificateById`).
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_FILE_SIZE`).

### 23. Database Naming Conventions
- Tables: lower_snake_case, plural (e.g., `users`, `certificate_requests`).
- Columns: lower_snake_case (e.g., `first_name`, `created_at`).

### 24. API Naming Conventions
- Resource-based, lowercase, nouns (e.g., `/user/requests`).

### 25. Error Handling Strategy
`@ControllerAdvice` for global exception handling. Custom exceptions (`ResourceNotFoundException`). Custom Error pages (`404.html`, `500.html`).

### 26. Logging Strategy
SLF4J + Logback. Info level for general flows, Error for exceptions. Logs written to console and rolling file.

### 27. Environment Configuration Strategy
`application.properties` (default), `application-dev.properties` (local dev), `application-prod.properties` (Docker, injected via ENV vars).

### 28. Backup/Storage Strategy
- Uploaded docs and PDFs stored in a local directory `/data/uploads` mounted as a Docker volume.

### 29. Certificate Generation Design
Thymeleaf template compiled to HTML -> HTML to PDF using OpenPDF/iText. Unique UUID embedded as text and generated QR Code image.

### 30. Certificate Verification Design
QR code points to `https://[domain]/verify/{uuid}`. Controller looks up UUID and returns public details if valid.

---

## SINGLE SOURCE OF TRUTH (CONTRACT)

### ENUM VALUES
**Role**: `USER`, `ADMIN`
**RequestStatus**: `PENDING`, `APPROVED`, `REJECTED`

### ENTITY NAMES AND FIELDS
**User**
- `id` (Long, PK)
- `email` (String, Unique)
- `password` (String)
- `firstName` (String)
- `lastName` (String)
- `role` (Enum Role)

**CertificateType**
- `id` (Long, PK)
- `name` (String, Unique)
- `description` (String)

**CertificateRequest**
- `id` (Long, PK)
- `user` (ManyToOne User)
- `certificateType` (ManyToOne CertificateType)
- `status` (Enum RequestStatus)
- `supportingDocumentPath` (String)
- `rejectionReason` (String)
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**Certificate**
- `id` (Long, PK)
- `certificateRequest` (OneToOne CertificateRequest)
- `uuid` (String, Unique, UUID format)
- `pdfPath` (String)
- `issuedAt` (LocalDateTime)

### URL PATTERNS (Thymeleaf/MVC)
- Public: `/`, `/login`, `/register`, `/verify/{uuid}`
- User: `/user/dashboard`, `/user/requests`, `/user/requests/new`, `/user/requests/{id}`
- Admin: `/admin/dashboard`, `/admin/requests`, `/admin/requests/{id}`, `/admin/users`

### ENVIRONMENT VARIABLES
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `APP_PORT`
