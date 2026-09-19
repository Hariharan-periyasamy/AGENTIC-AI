# DEMONSTRATION & PRESENTATION GUIDE

Follow this script for a flawless Viva / Lab demonstration.

**1. Automated Infrastructure Boot**
- Open Windows Terminal at the project root.
- Execute: `scripts\devops.bat`
- *Talking Point*: Explain how the script dynamically checks for prerequisites (Java, Maven, Docker), builds the application, executes 42+ unit and integration tests, packages the JAR, builds a lightweight Alpine Docker image, deploys it via Compose, and aggressively polls the health endpoint before reporting success.

**2. Application Workflow (User Side)**
- Open browser to `http://localhost:8080/register`.
- Register a student and Login.
- Go to **My Requests** -> **New Request**. Upload a dummy PDF. Submit.
- *Talking Point*: Explain Spring Security's CSRF protection and `@PreAuthorize` isolation ensuring users only see their own database rows.

**3. Administrative Workflow (Admin Side)**
- Open an incognito browser to `http://localhost:8080/login`.
- Login as `admin@example.com` / `admin`.
- Navigate to the pending request.
- Mark as **Under Review**. Then **Approve**.
- *Talking Point*: Explain the State Machine and how Approval triggers the `CertificateGenerationService` (iText PDF generation).

**4. Continuous Integration (Jenkins & Ansible)**
- Show the instructor the `Jenkinsfile`.
- *Talking Point*: Explain the Declarative Pipeline. Show how `ansible-playbook site.yml` is invoked during the Deployment stage to guarantee idempotent, zero-downtime container cycling.
