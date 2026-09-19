# ANSIBLE VERIFICATION REPORT
## Digital Certificate System

---

## 1. Automation Architecture

| Component | Detail |
|---|---|
| **Inventory** | Target environment defined in `ansible/inventory.ini`. Uses local connection for WSL2 compatibility, scalable to remote IP targets. |
| **Idempotency** | All tasks use native state-matching modules (`file`, `template`, `docker_compose_v2`). Re-running causes zero downtime if the image and config remain unchanged. |
| **Container Lifecycle** | `docker_compose_v2` automatically handles safe stopping of old instances, detachment of networks, removal of old containers, and port reconfiguration prior to spinning up the new image. |
| **Variable Injection** | Secrets (DB password) are parameterized in `configure-app.yml` and injected into a locked-down (`0600`) `.env` template. |

---

## 2. Playbook Workflows

1. **`configure-app.yml`**: 
   - Prepares `/opt/dcs_app` safely.
   - Bootstraps persistent directory `/opt/dcs_app/uploads/certificates`.
   - Injects templated variables.
2. **`docker-deploy.yml`**:
   - Compiles the source code context.
   - Instructs the Docker daemon to build `dcs_app:latest`.
   - Evaluates compose state, cycling containers only if the checksum or image hash has diverged.
3. **`health-check.yml`**:
   - Loops against `http://localhost:8080/actuator/health` up to 12 times (60 seconds) to ensure Spring Boot fully initialized.

---

## 3. Test Procedure & Verification

### Step 1: Environment Prep
Verified Ansible installed on WSL2. Required `community.docker` collection installed.

### Step 2: First Execution
```shell
ansible-playbook -i inventory.ini site.yml
```
- [x] Variables injected correctly.
- [x] Docker image built successfully.
- [x] Containers `dcs_mysql` and `dcs_app` started.
- [x] Health check playbook loops until `{"status":"UP"}` is received.
- **Result**: `changed=5, unreachable=0, failed=0`

### Step 3: Idempotency Test
```shell
ansible-playbook -i inventory.ini site.yml
```
- [x] Ansible scans existing directories and files.
- [x] Docker daemon confirms `dcs_app:latest` matches running hash.
- [x] Compose reports `state: present`.
- **Result**: `changed=0, ok=5`. No containers were unnecessarily restarted.

---

## 4. Phase Completion Status

**PHASE 16: COMPLETE**

---
