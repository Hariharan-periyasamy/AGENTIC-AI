# JENKINS SETUP GUIDE
## Digital Certificate System

This guide outlines how to configure a fresh Jenkins installation on a Windows host to support the Digital Certificate System CI/CD pipeline.

---

### 1. Prerequisite Installations (Windows Host)
1. **Jenkins**: Installed and running as a Windows Service.
2. **Docker Desktop**: Installed and running (ensure the Jenkins service account has permissions to use the `docker` CLI, or run Jenkins as a user with Docker access).
3. **Git**: Installed and available in the system PATH.

---

### 2. Global Tool Configuration

Navigate to **Manage Jenkins > Tools**.

#### 2.1. JDK Configuration
- **Name**: `Java 21` (Must match exactly what is in the `Jenkinsfile`)
- **Install automatically**: Uncheck.
- **JAVA_HOME**: Provide the absolute path to your Java 21 JDK (e.g., `C:\Program Files\Java\jdk-21`).

#### 2.2. Maven Configuration
- **Name**: `Maven 3.9`
- **Install automatically**: Check (or uncheck and point to `MAVEN_HOME` e.g., `C:\apache-maven-3.9.6`).

#### 2.3. Git Configuration
- **Name**: `Default`
- **Path to Git executable**: `C:\Program Files\Git\bin\git.exe`

---

### 3. Credential Management

Navigate to **Manage Jenkins > Credentials > System > Global credentials**.

1. **Git Credentials**: 
   - Kind: `Username with password` (or SSH username with private key).
   - ID: `git-credentials`
   - Enter your repository credentials.

2. **Docker/DB Secrets (Optional for Advanced Setup)**:
   - Kind: `Secret text`
   - IDs: `DB_PASSWORD`, `DB_ROOT_PASSWORD`. (The `docker-compose.yml` uses `.env` defaults for local testing, but in Jenkins, you can inject these secrets into the environment).

---

### 4. Required Jenkins Plugins
Navigate to **Manage Jenkins > Plugins**. Ensure the following are installed:
- Pipeline
- Git Plugin
- JUnit Plugin (for parsing `surefire-reports`)
- Workspace Cleanup Plugin (optional but recommended)
