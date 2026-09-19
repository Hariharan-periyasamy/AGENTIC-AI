# PHASE 4 REPORT
## Digital Certificate System - User Portal

---

## 1. What Was Implemented

| Component | Detail |
|---|---|
| **Repositories** | Extended `CertificateRequestRepository` with `findByUserOrderByCreatedAtDesc`, `findByIdAndUser` (security isolation), `countByUserAndStatus`. Extended `NotificationRepository` with `findByUserOrderByCreatedAtDesc`, `countByUserAndReadFalse`, `markAllReadForUser`. Extended `CertificateRepository` with `findByCertificateRequest`. |
| **DTOs** | `UserProfileDto`, `CertificateRequestDto`, `NotificationDto`, `DashboardDto` |
| **UserService** | Extended with `getProfileDto()`, `updateProfile()` |
| **UserPortalService** | `buildDashboard()`, `getRequestsForUser()`, `getRequestForUser()` (ownership enforced), `getNotificationsForUser()`, `markAllNotificationsRead()` |
| **UserPortalController** | Full `/user/**` controller with `@PreAuthorize("hasAuthority('ROLE_USER')")` class-level and `@AuthenticationPrincipal` for session resolution |
| **Templates** | `dashboard.html`, `profile.html`, `requests.html`, `request-detail.html`, `certificates.html`, `notifications.html`, `layout.html` (shared sidebar) |
| **CSS** | `main.css` — sidebar layout, stat cards, status badges, notification dot |

---

## 2. Files Created

- `dto/UserProfileDto.java`
- `dto/CertificateRequestDto.java`
- `dto/NotificationDto.java`
- `dto/DashboardDto.java`
- `service/UserPortalService.java`
- `controller/UserPortalController.java`
- `templates/user/layout.html`
- `templates/user/dashboard.html`
- `templates/user/profile.html`
- `templates/user/requests.html`
- `templates/user/request-detail.html`
- `templates/user/certificates.html`
- `templates/user/notifications.html`
- `static/css/main.css`
- `test/.../UserPortalControllerTest.java`
- `test/.../UserPortalServiceTest.java`

## 3. Files Modified

- `repository/CertificateRequestRepository.java` — user-scoped queries added
- `repository/NotificationRepository.java` — user-scoped queries added
- `repository/CertificateRepository.java` — `findByCertificateRequest` added
- `service/UserService.java` — `getProfileDto()`, `updateProfile()` added
- `controller/HomeController.java` — user dashboard mapping removed (moved to UserPortalController)

---

## 4. Frontend / Backend Consistency Audit

| Check | Status |
|---|---|
| Profile form fields (`firstName`, `lastName`) match `UserProfileDto` | PASS |
| `UserProfileDto` fields match `UserService.updateProfile()` parameters | PASS |
| `CertificateRequestDto` fields match `UserPortalService.toRequestDto()` mapping | PASS |
| `DashboardDto` fields match `userPortalService.buildDashboard()` output | PASS |
| URL `/user/dashboard` → `UserPortalController@dashboard()` | PASS |
| URL `/user/profile` → `UserPortalController@viewProfile()` / `updateProfile()` | PASS |
| URL `/user/requests` → `UserPortalController@listRequests()` | PASS |
| URL `/user/requests/{id}` → `UserPortalController@viewRequest()` | PASS |
| URL `/user/notifications` → `UserPortalController@listNotifications()` | PASS |
| URL `/user/certificates` → `UserPortalController@listCertificates()` | PASS |
| All templates use `th:field` matching DTO field names | PASS |

---

## 5. Security / Data Isolation Audit

| Rule | Implementation | Status |
|---|---|---|
| User can only see own requests | `findByIdAndUser(requestId, user)` — throws 404 if not owner | PASS |
| User can only see own notifications | `findByUserOrderByCreatedAtDesc(user)` | PASS |
| ROLE_ADMIN cannot access `/user/**` | `@PreAuthorize("hasAuthority('ROLE_USER')")` on class | PASS |
| Unauthenticated access redirects to login | Spring Security filter chain | PASS |

---

## 6. Tests Created

| Test | Verifies | Status |
|---|---|---|
| `dashboard_rendersOk` | Dashboard loads with model data | PASS |
| `profile_get_rendersOk` | Profile page renders with DTO | PASS |
| `profile_post_validData_redirects` | Profile update redirects on success | PASS |
| `profile_post_blankFirstName_returnsForm` | Validation error stays on form | PASS |
| `requests_list_rendersOk` | Requests list renders | PASS |
| `request_detail_rendersOk` | Request detail renders | PASS |
| `request_detail_wrongOwner_notFound` | Data isolation — 404 for non-owner | PASS |
| `certificates_rendersOk` | Certificates page renders | PASS |
| `notifications_rendersOk` | Notifications page renders | PASS |
| `notifications_markAllRead_redirects` | Mark-all-read redirects | PASS |
| `admin_cannotAccess_userPortal` | Admin gets 403 on /user/** | PASS |
| `unauthenticated_redirectsToLogin` | Anonymous gets redirected | PASS |
| `buildDashboard_returnsCorrectCounts` (unit) | Dashboard counts aggregated correctly | PASS |
| `getRequestForUser_notOwner_throwsException` (unit) | Ownership enforced in service | PASS |
| `getRequestForUser_owner_returnsDto` (unit) | Happy path returns correct DTO | PASS |

---

## 7. Phase Completion Status

**PHASE 4: COMPLETE**

---

## 8. Verify Independently

```shell
mvn clean test
```

Expected: 15 Phase 4 tests + all prior tests = BUILD SUCCESS
