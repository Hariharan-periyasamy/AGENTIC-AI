# Final Test Matrix

| Component | Status | Tool/Method | Notes |
|---|---|---|---|
| Registration Flow | PASS | JUnit/MockMvc | Passwords match, email unique, role assigned |
| Login Authentication | PASS | Spring Security Test | Admin and User credentials work |
| User Dashboard | PASS | MockMvc | Validates layout and model attributes |
| Admin Dashboard | PASS | MockMvc | Only accessible by ROLE_ADMIN |
| Certificate Request Submit | PASS | MockMvc | Multipart upload and DB save work |
| Certificate Review Flow | PASS | Service Tests | Admin can mark UNDER_REVIEW, APPROVE, or REJECT |
| Certificate Generation | PASS | Integration Test | PDF generation triggers on APPROVE |
| Public Certificate Verification | PASS | MockMvc API | API correctly returns validation status |
| Database Seeders | PASS | App Startup | Roles, admin user, and certificate types seed correctly |
| File Storage System | PASS | Unit Tests | Storage creates directories and limits file size/types |
| Audit Logs | PASS | Service Tests | Annotations create correct logs in separate transaction |
| Notifications | PASS | Integration Tests | Users receive updates on status changes |
| Cross-user Isolation | PASS | Spring Security/Services | Users cannot view or cancel others' requests |
| View Rendering (Thymeleaf) | PASS | MockMvc | No TemplateProcessingException; #request correctly replaced |
| Docker / Jenkins / Ansible | PASS | Static Verification | Configuration files syntax and paths verified |
| Windows DevOps Scripts | PASS | Script Review | Maven detection fixed for .bat files |

## Security Highlights

* Passwords correctly hashed via BCryptPasswordEncoder
* CSRF protections verified in all POST forms
* Method-level security (@PreAuthorize) covers administrative routes
* Cross-user Data isolation verified via Service-level query logic
* Global Model Attributes secure access without exposing raw Servlet instances in templates
