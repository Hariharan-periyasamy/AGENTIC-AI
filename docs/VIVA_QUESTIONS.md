# DEVOPS & SPRING BOOT VIVA QUESTIONS

**Q1: How did you implement Continuous Integration in this project?**
*Answer*: We wrote a declarative `Jenkinsfile`. Whenever code is pushed to GitHub, a Webhook triggers the Jenkins pipeline. Jenkins checks out the code, executes `mvn clean test`, and if successful, packages the `.jar` file.

**Q2: What is the role of Ansible in your pipeline?**
*Answer*: Ansible handles Continuous Deployment (CD). After Jenkins builds the Docker image, it runs an Ansible playbook (`site.yml`). The playbook uses the `community.docker` module to evaluate the state of the production containers. It idempotently stops the old container and boots the new one without breaking the database volume.

**Q3: How does your application ensure that User A cannot see User B's certificate request?**
*Answer*: We implemented Data Isolation using Spring Security (`@PreAuthorize`) and Spring Data JPA. When a user requests to view `ID=5`, the `UserPortalService` calls `requestRepository.findByIdAndUser(id, currentUser)`. If the request doesn't belong to the logged-in user, it throws an exception.

**Q4: How do you persist the database when a Docker container is destroyed?**
*Answer*: In our `docker-compose.yml`, we mapped a Docker volume `mysql_data:/var/lib/mysql`. Even if `docker compose down` is called, the volume persists on the host. When spun back up, MySQL reattaches to that volume.

**Q5: What happens if a test fails during the Jenkins pipeline?**
*Answer*: The `mvn test` step will return a non-zero exit code. Jenkins detects this, immediately aborts the pipeline, publishes the JUnit test report, and prevents the broken code from being packaged or deployed to Docker.
