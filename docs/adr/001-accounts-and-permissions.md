# ADR 001: Accounts, sessions, and request ownership

Date: 2026-10-21

Status: accepted design. Account persistence and password hashing are implemented; HTTP authentication, request ownership, and permission enforcement are planned next.

## Context

The current local demo exposes a shared request list. A usable service desk must distinguish the person requesting help from staff resolving it. Ownership checks must apply to list, search, details, and mutations, not just hide Angular controls.

## Permissions contract (target behavior)

| Operation | Anonymous | Requester | Staff |
| --- | --- | --- | --- |
| Register a requester account | Allowed when registration ships | Allowed | Allowed |
| Log in / obtain CSRF token | Allowed | Allowed | Allowed |
| View current session / log out | No session / 401 | Own session | Own session |
| Create a request | 401 | Allowed, owned by authenticated user | Allowed, owned by authenticated user |
| List/search requests | 401 | Own requests only | All requests |
| View request details | 401 | Own requests only | All requests |
| Change request status | 401 | 403 | Allowed |
| Assign a request (future) | 401 | 403 | Allowed, assignee must be active staff |
| Read/add comments (future) | 401 | Own requests only | All requests |
| Grant staff privileges | Denied | Denied | No public role-change endpoint |

An authenticated requester probing another person's request ID should receive 404, consistently across read/comment operations, to avoid disclosing its existence. Lists must filter ownership in the database before pagination and counts. A requester attempting a staff-only operation receives 403 regardless of whether the target exists. Staff access is for one organization; multi-tenant isolation is outside this release.

## Account data (implemented)

`accounts` contains UUID, canonical email (254 characters), display name (100 characters), password hash (255 characters), role (`REQUESTER` or `STAFF`), enabled flag, and creation timestamp. Migration `V2` adds the table without rewriting `V1` or modifying existing requests.

`CreateAccount` trims display names and strips/lowercases emails with `Locale.ROOT`. This application deliberately treats email addresses case-insensitively, including the local part. PostgreSQL enforces canonical storage and a unique email constraint. The internal service validates its command even when it is called without an HTTP controller.

`AccountService.createRequester` chooses `REQUESTER` itself; its input has no role or enabled field. Staff provisioning is a future restricted administrative process, not a registration option. No seed users or default passwords are added. `AccountResponse` contains profile fields only. Entities remain internal to the persistence layer, and their password-hash accessor has package visibility. `CreateAccount.toString()` redacts credentials to reduce accidental logging.

The service reports an existing email before hashing. PostgreSQL's unique constraint is authoritative if concurrent calls pass that precheck. A raced duplicate currently propagates `DataIntegrityViolationException`; the future registration endpoint must translate that specific unique constraint into the same duplicate-account response, without returning raw database errors. The HTTP status and anti-enumeration policy will be finalized with registration.

## Password storage (implemented)

Use Spring Security's crypto module with a `DelegatingPasswordEncoder` and BCrypt work factor 12. Stored values include the `{bcrypt}` prefix, allowing future algorithm upgrades. Only the crypto module is added in this milestone; the web security filter chain belongs to the login/authorization milestone.

Passwords must contain at least 12 Unicode code points, must not be entirely whitespace, and must fit in 72 UTF-8 bytes. The byte limit is enforced before hashing because BCrypt has a 72-byte input limit. Passwords are never trimmed or normalized. Two equal passwords produce different salted hashes. Tests verify both successful matching and wrong-password rejection with the production encoder.

Work factor 12 is a starting point, not a measured deployment guarantee. Benchmark login latency on the deployment hardware and adjust the work factor before public release. Password reset, email verification, breached-password checks, and rate limiting are future work. See [Spring password storage documentation](https://docs.spring.io/spring-security/reference/7.0/features/authentication/password-storage.html).

## Sessions and CSRF (planned)

Use server-managed Spring Security sessions for the same-origin Angular application. Session identifiers stay in HttpOnly cookies; no JWT or credential storage in localStorage. Use Secure cookies in HTTPS deployments, SameSite=Lax, session fixation protection on login, and session invalidation on logout.

Keep CSRF protection enabled for state-changing requests, including login/logout. Proposed endpoints are `GET /api/auth/csrf`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`, and a requester-registration endpoint once its error/abuse policy is defined. These endpoints **do not exist yet**. Align the readable CSRF cookie/header with Angular's XSRF support, obtain a fresh token after authentication/logout, and verify the actual behavior in API and browser tests.

Return JSON Problem Details for unauthenticated/forbidden API calls rather than HTML redirects. Login errors must use a generic invalid-credentials message for unknown users, wrong passwords, and disabled accounts.

## Request ownership migration (planned)

Add a `created_by` foreign key in a new migration when enforcing authentication. New requests derive ownership from the authenticated principal, never a client-supplied owner ID. Existing V1 demo requests have no owner; introduce a nullable column and let staff see legacy requests while requester queries require their own account ID. Do not assign legacy data to the first person who registers. Restrict legacy transitions to staff and decide explicit archival/backfill before making ownership non-null.

## Verification requirements

This milestone: normalization, validation before hashing, no caller-selected role, BCrypt salting/matching, byte-limit boundaries, persisted hash reloading, and database uniqueness/canonical-email constraints. Rebuild Compose against its existing data volume to verify `V1 → V2` startup without data loss.

Next milestone: successful/failed login, disabled users, CSRF rejection, 401/403/404 behavior, cross-user access through detail/list/search/count, session fixation/logout behavior, and Angular expired-session handling. Do not publish the current demo as an authenticated product before these checks pass.
