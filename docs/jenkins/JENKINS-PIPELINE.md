# JENKINS PIPELINE ARCHITECTURE
## Digital Certificate System

The `Jenkinsfile` at the root of the repository defines a declarative CI/CD pipeline optimized for a Windows environment (`bat` commands instead of `sh`).

---

## 1. Pipeline Stages

1. **Checkout**: 
   - Pulls the latest source code from the Git repository.
2. **Compile**: 
   - Executes `mvn clean compile` to verify syntax, dependencies, and resolve compilation errors early.
3. **Unit Test**: 
   - Executes `mvn test -DskipITs=true`.
   - Uses the JUnit plugin to publish XML test results from `target/surefire-reports/*.xml`. If tests fail, the pipeline aborts.
4. **Integration Test**: 
   - Executes `mvn test -Dtest=*IntegrationTest` to specifically run the complex, database-heavy MockMvc workflows (`CompleteWorkflowIntegrationTest`).
5. **Package**: 
   - Executes `mvn package -DskipTests` to package the Spring Boot JAR file.
6. **Docker Build**: 
   - Uses `docker compose build` to construct the multi-stage Alpine Dockerfile.
7. **Docker Validation**: 
   - Executes `docker images | findstr dcs_app` to strictly verify the image was created and cached locally.
8. **Deployment**: 
   - Executes `docker compose up -d` to spin up MySQL and the Spring Boot application in detached mode. Pauses for 30 seconds to allow JVM and DB initialization.
9. **Health Check**: 
   - Executes a `curl` request against `http://localhost:8080/actuator/health`. If it fails, the stage fails.

---

## 2. Post-Build Actions

- **Success**: Prints success message indicating application is live.
- **Failure**: If *any* stage fails (e.g., tests fail, or health check fails), Jenkins triggers the `failure {}` block which executes `docker compose down`. This acts as an automated rollback, cleaning up broken containers.

---

## 3. Creating the Pipeline Job

1. Go to Jenkins Dashboard > **New Item**.
2. Enter Name: `Digital-Certificate-System-Pipeline`.
3. Select **Pipeline** and click OK.
4. Scroll to the **Pipeline** section:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: Enter your git URL.
   - Credentials: Select `git-credentials`.
   - Branch Specifier: `*/main` (or `*/master`)
   - Script Path: `Jenkinsfile`
5. Click **Save** and **Build Now**.
