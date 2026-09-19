# Ansible Automation for Digital Certificate System

This directory contains the Ansible playbooks required to automatically provision, configure, and deploy the application in a production-like environment using Docker.

## Playbook Structure
- `inventory.ini`: Defines target servers. Defaults to `localhost` for local/WSL deployment.
- `site.yml`: Master playbook that imports all subsequent playbooks in order.
- `configure-app.yml`: Prepares directories, copies `docker-compose.yml`, and securely injects environment variables.
- `docker-deploy.yml`: Builds the Docker image, handles container lifecycle (stops old, removes safely, starts new), and attaches persistence/ports idempotently.
- `health-check.yml`: Actively polls the `/actuator/health` endpoint to verify successful boot.

## Windows / WSL2 Instructions

Ansible is a Linux-native tool. To run this on Windows, you must use **Windows Subsystem for Linux (WSL2)**.

### 1. Prerequisites in WSL2 (Ubuntu)
Open your WSL2 terminal and install Ansible and Docker prerequisites:
```bash
sudo apt update
sudo apt install -y ansible python3-pip
pip3 install docker docker-compose
ansible-galaxy collection install community.docker
```

### 2. Execution
Navigate to the mounted Windows directory inside WSL (e.g., `/mnt/c/Users/.../DIGITAL-CERTIFICATE-SYSTEM/ansible`).

Run the complete orchestration:
```bash
ansible-playbook -i inventory.ini site.yml --ask-become-pass
```

### Idempotency
These playbooks are fully idempotent. If you run the command twice without changing code or variables, Ansible will report `changed=0`, meaning it verified the configuration without disrupting the running system.
