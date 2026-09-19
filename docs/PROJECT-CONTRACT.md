# DIGITAL CERTIFICATE REQUEST AND APPROVAL SYSTEM
## PROJECT CONTRACT & SPECIFICATION

**1. ARCHITECTURE**
- **Backend**: Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Hibernate.
- **Frontend**: Thymeleaf, Bootstrap 5, HTML5.
- **Database**: MySQL 8.
- **CI/CD**: Jenkins Pipeline, Docker, Ansible.

**2. ENTITIES**
- `User`: id, firstName, lastName, email, password, roles.
- `Role`: id, name (`ROLE_USER`, `ROLE_ADMIN`).
- `CertificateType`: id, name, description.
- `CertificateRequest`: id, user, certificateType, purpose, remarks, supportingDocumentPath, status, rejectionReason, createdAt, updatedAt.
- `Certificate`: id, request, certificateNumber, issueDate, verificationCode, uuid.
- `Notification`: id, user, message, isRead, createdAt.
- `AuditLog`: id, action, performedBy, details, timestamp.

**3. STATE MACHINE (CertificateRequest)**
- PENDING -> UNDER_REVIEW
- UNDER_REVIEW -> APPROVED
- UNDER_REVIEW -> REJECTED
- PENDING -> CANCELLED
