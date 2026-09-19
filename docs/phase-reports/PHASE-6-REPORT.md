# PHASE 6 REPORT
## Digital Certificate System - Admin Request Management

---

## 1. What Was Implemented

| Component | Detail |
|---|---|
| **Repositories** | Extended `CertificateRequestRepository` with `searchByApplicantKeyword` and `searchByStatusAndApplicantKeyword` to support admin search features. |
| **DTOs** | Created `AdminDashboardDto`, `AdminRequestListDto`, `AdminRequestReviewDto`. |
| **Services** | `AdminService` handling business rules for review, approve, reject. `CertificateGenerationService` created as a stub for Phase 7 triggering. |
| **Controllers** | `AdminController` with endpoints `/admin/dashboard`, `/admin/requests`, `/admin/requests/{id}`, `/admin/requests/{id}/approve`, `/admin/requests/{id}/reject`. Restricted to `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`. |
| **Templates** | Admin specific layout (`layout.html` with dark green theme), `dashboard.html`, `requests.html` (with search form), and `request-detail.html` (with applicant/document details and action buttons). |

---

## 2. Features Verified

* **Dashboard**: Displays aggregated request counts by status.
* **Search and Filter**: Admin can filter requests by status and/or search applicant names/emails.
* **Auto-Review State**: Opening a `PENDING` request automatically transitions it to `UNDER_REVIEW`.
* **State Machines (Rules Enforced)**:
  - Cannot approve/reject a `CANCELLED` request.
  - Cannot approve an already `APPROVED` or `REJECTED` request.
  - Rejection reason is *strictly mandatory* (validated in service).
* **Consequential Actions**:
  - Approval triggers `CertificateGenerationService` (Phase 7 stub).
  - Approval/Rejection triggers user notifications via `NotificationRepository`.
  - Admin actions are logged securely via `AuditLogService`.

---

## 3. Files Created/Modified

**Created:**
- `dto/admin/AdminDashboardDto.java`, `AdminRequestListDto.java`, `AdminRequestReviewDto.java`
- `service/AdminService.java`, `CertificateGenerationService.java`
- `controller/AdminController.java`
- `templates/admin/layout.html`, `dashboard.html`, `requests.html`, `request-detail.html`
- `test/../admin/AdminServiceTest.java`, `AdminControllerTest.java`

**Modified:**
- `repository/CertificateRequestRepository.java` (added search queries)
- `repository/UserRepository.java` (added findByEmail)
- `controller/HomeController.java` (removed duplicate admin dashboard mapping)

---

## 4. Test Coverage

| Test Area | Details |
|---|---|
| **Admin Access Rules** | Non-admins get 403 Forbidden. Admins get 200 OK. |
| **State Transitions** | `markUnderReview` updates status. |
| **Approval Flow** | `approveRequest_valid` updates status, notifies, logs, generates cert. |
| **Rejection Flow** | `rejectRequest_valid` updates status, sets reason, notifies, logs. |
| **Rule Violations** | `missing reason`, `already approved`, `cancelled` all throw `InvalidRequestOperationException`. |

---

## 5. Phase Completion Status

**PHASE 6: COMPLETE**

---

## 6. Verify Independently

```shell
mvn clean test
```

Expected: 14 admin tests added + prior tests = BUILD SUCCESS
