# PHASE 5 REPORT
## Digital Certificate System - Certificate Request Workflow

---

## 1. What Was Implemented

| Component | Detail |
|---|---|
| **Entities** | Updated `RequestStatus` with `UNDER_REVIEW`, `CANCELLED`. Added `purpose`, `remarks`, `supportingDocumentPath` to `CertificateRequest`. |
| **Repositories** | Added duplicate check query to `CertificateRequestRepository` based on status (`PENDING`, `UNDER_REVIEW`) and certificate type. |
| **Services** | `FileStorageService` for secure file storage (UUID renaming, size limits, type validation). `AuditLogService` with `REQUIRES_NEW` transactions. `UserPortalService` extended with `submitRequest()`, `cancelRequest()`. |
| **Controllers** | `UserPortalController` extended with GET/POST `/requests/new`, POST `/requests/{id}/cancel`. File upload processing. |
| **DTOs** | Created `CertificateRequestSubmitDto` (validation annotations). Updated `CertificateRequestDto` with new fields and `cancellable` flag. |
| **Templates** | Created `new-request.html`. Updated `request-detail.html` (added new fields and Cancel button). Updated `requests.html` (New Request button). |
| **Exceptions** | `FileStorageException`, `DuplicateRequestException`, `InvalidRequestOperationException`. Caught by `GlobalExceptionHandler`. |

---

## 2. Features Verified

* **Request Validation**: DTO annotations validate required fields (purpose, type).
* **Duplicate Prevention**: Rejects if user has active (`PENDING` or `UNDER_REVIEW`) request for the same certificate type.
* **File Validation**: `FileStorageService` enforces maximum 5MB size and PDF/JPG/PNG extensions.
* **File Storage**: Saves file to disk with secure UUID name to prevent path traversal/overwrite, returning relative path.
* **Audit Logging**: Logs `SUBMIT_REQUEST` and `CANCEL_REQUEST` events independently.
* **Request Cancellation**: Users can cancel requests *only* if status is `PENDING`.
* **Security Isolation**: Users cannot cancel or view other users' requests.

---

## 3. Files Created/Modified

**Created:**
- `service/FileStorageService.java`
- `service/AuditLogService.java`
- `exception/FileStorageException.java`, `DuplicateRequestException.java`, `InvalidRequestOperationException.java`
- `dto/CertificateRequestSubmitDto.java`
- `templates/user/new-request.html`
- `templates/error/400.html`
- `test/../controller/CertificateRequestWorkflowTest.java`
- `test/../service/FileStorageServiceTest.java`
- `test/../service/AuditLogServiceTest.java`

**Modified:**
- `entity/enums/RequestStatus.java`
- `entity/CertificateRequest.java`
- `repository/CertificateRequestRepository.java`
- `exception/GlobalExceptionHandler.java`
- `dto/CertificateRequestDto.java`
- `service/UserPortalService.java`
- `controller/UserPortalController.java`
- `templates/user/request-detail.html`, `templates/user/requests.html`

---

## 4. Test Coverage

| Test Area | Details |
|---|---|
| **Form Display** | `getNewRequestForm_rendersOk` |
| **Submission** | `submitRequest_validData_redirectsToRequests`, `submitRequest_missingDocument_returnsForm` |
| **Validation** | `submitRequest_invalidFile_throwsFileStorageException`, `submitRequest_duplicateRequest_returnsFormWithError` |
| **Cancellation** | `cancelRequest_validPendingRequest_redirectsWithSuccess`, `cancelRequest_invalidState_redirectsWithError` |
| **File Storage Unit** | Valid upload, invalid extension, empty file rejections |
| **Audit Unit** | Verifies database save execution via mock repository |

---

## 5. Phase Completion Status

**PHASE 5: COMPLETE**

---

## 6. Verify Independently

```shell
mvn clean test
```

Expected: 21 tests added/updated + all prior tests = BUILD SUCCESS
