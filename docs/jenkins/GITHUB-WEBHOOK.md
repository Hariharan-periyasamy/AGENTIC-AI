# GITHUB WEBHOOK INTEGRATION
## Digital Certificate System

This guide outlines how to configure GitHub to automatically trigger the Jenkins CI/CD pipeline whenever new code is pushed to the repository.

---

## 1. Jenkins Configuration (The Receiver)

Before configuring GitHub, Jenkins must be prepared to receive the webhook.

1. **Install GitHub Plugin**: Ensure the `GitHub plugin` is installed in Jenkins (Manage Jenkins > Plugins).
2. **Expose Jenkins to the Internet**: If your Jenkins instance is running locally on Windows, GitHub cannot reach it. You must use a tool like **ngrok** to expose your local Jenkins port (e.g., `ngrok http 8080`). Note the forwarding URL (e.g., `https://abcdef123.ngrok.app`).
3. **Configure Job Trigger**:
   - Open your `Digital-Certificate-System-Pipeline` job.
   - Click **Configure**.
   - Under **Build Triggers**, check the box for **GitHub hook trigger for GITScm polling**.
   - Save the configuration.

---

## 2. GitHub Configuration (The Sender)

1. Navigate to your project repository on GitHub.
2. Go to **Settings > Webhooks**.
3. Click **Add webhook**.
4. **Payload URL**: Enter your Jenkins URL followed by `/github-webhook/`. 
   - *Example*: `https://abcdef123.ngrok.app/github-webhook/`
5. **Content type**: Select `application/json`.
6. **Secret**: (Optional but recommended). If configured, you must add the corresponding secret text credential in Jenkins.
7. **Which events would you like to trigger this webhook?**: 
   - Select **Just the push event.**
8. Ensure **Active** is checked.
9. Click **Add webhook**.

---

## 3. Webhook Test Procedure

Follow these steps to verify the end-to-end automation.

### Step 1: Modify Application
Make a trivial change to the application. For example, add a comment to `src/main/resources/application.properties`:
```properties
# Webhook test comment
```

### Step 2: Commit
```shell
git add src/main/resources/application.properties
git commit -m "chore: test github webhook"
```

### Step 3: Push
```shell
git push origin main
```

### Step 4: Verify GitHub Webhook
- Go to GitHub > Settings > Webhooks.
- Click on your configured webhook.
- Check the **Recent Deliveries** tab at the bottom.
- You should see a new entry with a green checkmark indicating a `200 OK` response from Jenkins.

### Step 5: Verify Jenkins Build
- Open your Jenkins Dashboard.
- A new build for `Digital-Certificate-System-Pipeline` should have automatically spawned in the queue and started executing.

### Step 6: Verify Tests
- Monitor the Jenkins console output.
- Verify that the `Unit Test` and `Integration Test` stages execute and pass successfully.

### Step 7: Verify Docker Build
- Continue monitoring the console output.
- Verify the `Docker Build` stage executes `docker compose build`.
- Verify the `Deployment` stage spins up the containers and the `Health Check` stage passes.

---

## 4. Troubleshooting & Diagnosis

If the automated trigger fails, consult these diagnostics:

### 4.1. Failed Webhook Diagnosis (GitHub Side)
- **Red "X" in Recent Deliveries**: GitHub could not reach Jenkins.
  - **404 Error**: Check that you appended `/github-webhook/` (with the trailing slash) to the Payload URL.
  - **Timeout/Unreachable**: If using ngrok, ensure the tunnel is still active and the URL hasn't changed. Check your firewall settings.
  - **403 Forbidden**: Jenkins security is blocking anonymous webhook requests. Go to Manage Jenkins > Security and ensure "Prevent Cross Site Request Forgery exploits" is configured to allow GitHub webhooks, or ensure API tokens are properly configured.

### 4.2. Diagnosis (Jenkins Side)
- **Webhook Received but Job Didn't Start**:
  - Ensure the repository URL in the Jenkins job configuration *exactly* matches the GitHub repository URL.
  - Ensure **GitHub hook trigger for GITScm polling** is checked in the job configuration.
  - Check **Manage Jenkins > System Log** and look for the `GitHubWebHook` logger to see if Jenkins ignored the payload.
