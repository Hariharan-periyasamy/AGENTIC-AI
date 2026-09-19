# SYSTEM TROUBLESHOOTING GUIDE

## 1. Port 8080 or 3306 is already in use
**Symptom**: `docker compose up` fails stating "bind: address already in use".
**Fix**: 
Run `scripts\stop.bat` to forcefully kill lingering Java processes.
If MySQL is running locally (XAMPP/WAMP), stop that service to free port 3306 for Docker.

## 2. Jenkins Webhook is not firing
**Symptom**: Code is pushed to GitHub but Jenkins doesn't start.
**Fix**: 
1. Check GitHub Settings > Webhooks > Recent Deliveries.
2. If it's a 404, ensure the URL ends with `/github-webhook/`.
3. If running locally, ensure `ngrok` is running and the URL is up-to-date in GitHub.

## 3. Database Schema Mismatch
**Symptom**: Exceptions related to missing columns upon startup.
**Fix**: 
Our `application.properties` uses `spring.jpa.hibernate.ddl-auto=update`. If the schema becomes irreparably broken during development, run `scripts\clean.bat`, explicitly answer `Y` to destroy the database volumes, and restart the application.

## 4. Ansible Idempotency Issues
**Symptom**: `docker-deploy.yml` always reports `changed=1` even when no code changed.
**Fix**: 
Ensure `target/` and `.git/` are excluded in the playbook's copy task. If timestamps on files change, Docker registers a new build context.
