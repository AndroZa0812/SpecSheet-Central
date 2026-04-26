# SonarQube Assessment Remediation Design

**Date:** 2026-04-26
**Project:** SpecSheet Central (Backend)
**SonarQube Key:** specsheet-central

## Context

SonarQube assessment revealed 4 areas needing attention:
- 1 MAJOR code smell (System.out in DataSeeder)
- 1 HIGH-probability security hotspot (CSRF disabled)
- 5 LOW-probability security hotspots (per-controller CORS)
- 0% test coverage

## Design Decisions

### 1. CSRF Disabled — Mark as SAFE (No code change)

The project uses stateless JWT authentication (no cookies, no sessions). CSRF protection is only necessary for cookie-based auth. Disabling CSRF is the correct and standard approach for JWT APIs.

**Action:** Mark SonarQube hotspot `AZ3GrjWDMMAbgOaviV-Z` as REVIEWED → SAFE with explanation: "Stateless JWT API; CSRF protection not applicable."

### 2. System.out in DataSeeder — Verify Fix

The SonarQube scan flagged `DataSeeder.java:239` for `System.out`. Current code uses `log.info()` (SLF4J). The issue likely existed in a prior version and may already be resolved.

**Action:** Verify no `System.out` calls remain. If still present, replace with logger. The issue should resolve on next SonarQube scan.

### 3. Centralize CORS Configuration

**Current state:** `@CrossOrigin(origins = "*")` on 5 controllers — scattered, permissive, flagged by SonarQube.

**Target state:** Single centralized CORS config in `WebConfig.java`, driven by environment variable.

**Implementation:**
- Remove `@CrossOrigin` annotations from: AuthController, CategoryController, FileController, OrderController, ProductController
- Remove unused `@CrossOrigin` import from each controller
- Add CORS configuration in `WebConfig.java` via `addCorsMappings(CorsRegistry)`
- Read allowed origins from `CORS_ALLOWED_ORIGINS` env var (comma-separated list)
- No hardcoded origins — defaults to empty (no CORS allowed) if env var not set
- Apply to all API paths (`/api/**`)
- Allow methods: GET, POST, PUT, PATCH, DELETE
- Allow headers: Authorization, Content-Type

**After code change:** Mark all 5 CORS hotspots as REVIEWED → FIXED in SonarQube.

### 4. Core Unit Tests

**Target:** Meaningful test coverage of critical business logic.

**Test categories:**

**Service unit tests** (Mockito):
- `ProductServiceTest` — findAll, findById, create, update, delete, updateStock, updateDatasheet
- `CategoryServiceTest` — findAll, findById, create, update, delete
- `OrderServiceTest` — create, findByUser, findAll, updateStatus
- `UserServiceTest` — register, findByEmail
- `ReviewServiceTest` — findByProduct, findByProductId, create

**Controller tests** (`@WebMvcTest` + MockMvc):
- `AuthControllerTest` — register, login
- `ProductControllerTest` — getAll (with filters), getById, create, update, delete
- `CategoryControllerTest` — CRUD
- `OrderControllerTest` — create, getMyOrders, getAll, updateStatus

**Test infrastructure:**
- H2 already in `pom.xml` as test dependency
- Use `spring-boot-starter-test` (already in pom)
- Use `@MockBean` / `@Mock` pattern for service layer in controller tests

## SonarQube Hotspot Review Actions

| Hotspot Key | File | Category | Action |
|---|---|---|---|
| AZ3GrjWDMMAbgOaviV-Z | SecurityConfig.java:34 | CSRF | REVIEWED → SAFE |
| AZ3GrjVUMMAbgOaviV-L | AuthController.java:16 | CORS | REVIEWED (to be FIXED by code change) |
| AZ3GrjVrMMAbgOaviV-O | CategoryController.java:13 | CORS | REVIEWED (to be FIXED by code change) |
| AZ3GrjVbMMAbgOaviV-M | FileController.java:12 | CORS | REVIEWED (to be FIXED by code change) |
| AZ3GrjVzMMAbgOaviV-P | OrderController.java:14 | CORS | REVIEWED (to be FIXED by code change) |
| AZ3GrjVjMMAbgOaviV-N | ProductController.java:13 | CORS | REVIEWED (to be FIXED by code change) |