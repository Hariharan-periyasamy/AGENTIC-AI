# FEATURE REGISTRY

## User Portal (`ROLE_USER`)
1. **Authentication**: Register, Login, Secure Session.
2. **Dashboard**: Metrics summary of user's active/total requests.
3. **Request Submission**: Select certificate type, enter purpose, upload PDF document, submit.
4. **Request Tracking**: View active requests, see dynamic status badges, cancel pending requests.
5. **Certificate Management**: Download approved auto-generated PDF certificates.
6. **Notifications**: In-app alerts for status changes.

## Admin Portal (`ROLE_ADMIN`)
1. **Request Management**: View all platform requests, filter by status.
2. **Review Workflow**: Mark as `UNDER_REVIEW`, inspect uploaded documents.
3. **Approval Engine**: Approve requests, automatically triggering secure PDF generation.
4. **Rejection Engine**: Reject requests requiring a mandatory explanation string.
5. **System Audit**: Global view of all immutable audit logs (registration, logins, generation).

## Public Portal
1. **Verification Code**: Validate physical/digital certificates via `/api/certificates/verify/{uuid}`.
