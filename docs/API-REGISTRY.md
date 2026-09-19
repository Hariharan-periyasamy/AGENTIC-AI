# API ENDPOINT REGISTRY

## Public Endpoints
| Method | Path | Purpose | Auth Required |
|---|---|---|---|
| GET | `/` | Redirects to login/dashboard | No |
| GET/POST | `/login` | Spring Security auth | No |
| GET/POST | `/register` | User registration | No |
| GET | `/verify` | Verification web UI | No |
| GET | `/api/certificates/verify/{code}` | JSON API verification | No |

## User Endpoints (`ROLE_USER`)
| Method | Path | Purpose |
|---|---|---|
| GET | `/user/dashboard` | Main student dashboard |
| GET | `/user/requests` | List user's requests |
| GET/POST | `/user/requests/new` | Submit new request |
| GET | `/user/requests/{id}` | View request details |
| POST | `/user/requests/{id}/cancel` | Cancel pending request |
| GET | `/user/certificates` | View generated certificates |
| GET | `/user/certificates/{id}/download` | Download PDF |
| GET | `/user/notifications` | View alerts |
| GET/POST | `/user/profile` | Edit user profile |

## Admin Endpoints (`ROLE_ADMIN`)
| Method | Path | Purpose |
|---|---|---|
| GET | `/admin/dashboard` | Main admin dashboard |
| GET | `/admin/requests` | List/Filter all requests |
| GET | `/admin/requests/{id}` | Review request details |
| POST | `/admin/requests/{id}/review` | Move to UNDER_REVIEW |
| POST | `/admin/requests/{id}/approve` | Approve & generate PDF |
| POST | `/admin/requests/{id}/reject` | Reject request |
| GET | `/admin/audit` | View system audit logs |
