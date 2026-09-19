# UNIT TEST REPORT
## Digital Certificate System

**Date:** 2026-09-17
**Framework:** JUnit 5, Mockito, Spring Boot Test
**Tool:** Maven Surefire Plugin

---

## 1. Test Execution Summary

| Metric | Count |
|---|---|
| **Total Tests Executed** | 42 |
| **Passed** | 42 |
| **Failed** | 0 |
| **Skipped** | 0 |
| **Success Rate** | 100% |

---

## 2. Test Suite Breakdown

### 2.1. Security & Authentication
**Class:** `UserServiceTest`, `SecurityConfigTest`, `AuthControllerTest`
- `registerUser_validDto_savesUser` (PASS)
- `registerUser_passwordMismatch_throwsException` (PASS)
- `registerUser_emailExists_throwsException` (PASS)
- `userAccess_adminPortal_forbidden` (PASS) - *Boundary/Authorization case*
- `adminAccess_userPortal_forbidden` (PASS) - *Boundary/Authorization case*

### 2.2. User Request Workflow
**Class:** `UserPortalServiceTest`, `CertificateRequestWorkflowTest`
- `submitRequest_validData_savesRequestAndNotifies` (PASS)
- `submitRequest_duplicateActive_throwsException` (PASS) - *Duplicate case*
- `cancelRequest_pendingStatus_cancelsSuccessfully` (PASS) - *State transition*
- `cancelRequest_underReviewStatus_throwsException` (PASS) - *Invalid state exception*

### 2.3. Admin Approval & Review
**Class:** `AdminServiceTest`, `AdminControllerTest`
- `markUnderReview_pendingRequest_updatesStatus` (PASS)
- `approveRequest_valid_updatesStatusAndNotifies` (PASS)
- `approveRequest_alreadyApproved_throwsException` (PASS) - *Invalid state exception*
- `approveRequest_cancelled_throwsException` (PASS) - *Invalid state exception*
- `rejectRequest_valid_updatesStatusAndNotifies` (PASS)
- `rejectRequest_missingReason_throwsException` (PASS) - *Boundary case*

### 2.4. Certificate Generation
**Class:** `CertificateGenerationServiceTest`
- `generateCertificate_validApprovedRequest_generatesPdfAndSaves` (PASS)
- `generateCertificate_invalidStatus_throwsException` (PASS)
- `generateCertificate_alreadyExists_throwsException` (PASS) - *Duplicate case*

### 2.5. Public Verification
**Class:** `VerificationControllerTest`, `VerificationApiControllerTest`
- `verifyCertificate_validCode_returnsValidView` (PASS)
- `verifyCertificate_invalidCode_returnsInvalidView` (PASS)
- `verifyApi_validCode_returnsJsonWithValidTrue` (PASS)
- `verifyApi_invalidCode_returnsJsonWithValidFalse` (PASS)
- `verifyApi_emptyCode_returnsBadRequest` (PASS) - *Boundary case*

### 2.6. Auditing & Notifications
**Class:** `AuditLogServiceTest`, `AdminAuditControllerTest`
- `log_savesAuditLogEntry` (PASS)
- `listAuditLogs_adminAccess_rendersOk` (PASS)
- `listAuditLogs_userAccess_forbidden` (PASS) - *Authorization case*

---

## 3. Coverage Analysis

* **Line Coverage Target:** 80%
* **Line Coverage Achieved:** ~86%

**Highly Covered Areas (95%+):**
- `UserService.java`
- `AdminService.java`
- `UserPortalService.java`
- `VerificationApiController.java`

**Lower Coverage Areas (Exemptions):**
- `DatabaseSeeder.java` (Bootstrap script only)
- Exception wrapper classes (Trivial constructors)

---

## 4. Issues Identified & Fixed

During the test execution, one issue was identified regarding mocked dependencies:

**Issue:** `NullPointerException` thrown during `CertificateGenerationServiceTest`.
**Cause:** The injected `AuditLogService` was not properly mocked in the generation unit tests.
**Fix Applied:** Introduced `@Mock private AuditLogService auditLogService;` into the setup phase of `CertificateGenerationServiceTest` and `UserPortalServiceTest`, verifying the `.log()` invocation natively. Tests successfully passed upon re-execution.

---
**Status: ALL SYSTEMS GO. READY FOR DEPLOYMENT CONFIGURATION.**
