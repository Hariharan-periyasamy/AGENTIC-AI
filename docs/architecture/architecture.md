# Digital Certificate Request and Approval System - Architecture

## 1. System Overview
The Digital Certificate Request and Approval System is a robust, full-stack enterprise application designed to manage the end-to-end lifecycle of digital certificates. It facilitates user requests, administrative approvals, secure PDF generation, and public verification.

## 2. Tech Stack
- **Backend**: Java 21, Spring Boot 3.x
- **Frontend**: Thymeleaf, Bootstrap, HTML5, CSS3, JavaScript
- **Database**: MySQL 8.0
- **Build Tool**: Maven 3.9.x
- **DevOps**: Git, Docker, Docker Compose, Jenkins, Ansible

## 3. High-Level Architecture
The system follows a classic N-Tier monolithic architectural pattern:
1.  **Presentation Layer**: Thymeleaf templates rendered server-side.
2.  **Controller Layer**: Spring MVC Controllers handling HTTP requests.
3.  **Service Layer**: Business logic and transaction management.
4.  **Data Access Layer**: Spring Data JPA Repositories interfacing with MySQL.

## 4. Key Components
-   **User Module**: Registration, login, profile management.
-   **Certificate Workflow Module**: Request submission, status tracking.
-   **Admin Module**: Dashboard, request review, approval/rejection.
-   **Generator Module**: PDF compilation, UUID injection, QR Code generation.
-   **Verification Module**: Public endpoint to verify certificate authenticity.

## 5. DevOps Pipeline
1.  **VCS**: GitHub.
2.  **CI**: Jenkins triggered via Webhook.
3.  **Build**: Jenkins runs mvn clean test package.
4.  **Containerization**: Docker builds an image.
5.  **Deployment**: Ansible provisions and starts Docker Compose.
