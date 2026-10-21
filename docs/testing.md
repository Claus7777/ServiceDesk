# Testing strategy

Test behaviors and failures that matter to a user, then exercise the real database and container path.

| Suite | Tool | What it protects |
| --- | --- | --- |
| `RequestServiceTest` | JUnit Jupiter + Mockito | New status, normalization, deterministic UTC times, missing requests, no-op updates |
| `RequestControllerTest` | MockMvc + Mockito | HTTP status/Location, invalid input, unknown enums, filter forwarding, Problem Details |
| `RequestApiIT` | Spring context + Testcontainers PostgreSQL | Flyway startup, JPA mappings, persisted status update, literal search, filtering, pagination validation |
| `AccountServiceTest` | JUnit Jupiter + Mockito + Bean Validation | Canonical email/name, requester-only creation, validation before hashing, duplicate precheck, UTF-8 password byte limits, credential redaction |
| `PasswordConfigurationTest` | Production BCrypt encoder | Salted hashes, work factor, correct/incorrect password matching |
| `AccountPersistenceIT` | Spring context + Testcontainers PostgreSQL | Reloaded profile/hash, database uniqueness, canonical-email constraints |
| `request-api.service.spec.ts` | Angular HTTP testing + Vitest | Query parameters, empty filters, PATCH contract |
| `app.component.spec.ts` | Angular TestBed + Vitest | Empty state, whitespace validation, duplicate submission, draft preservation on errors |
| `scripts/smoke.py` | Python HTTP client + Compose | Nginx serving and proxy, create/read/update/persistence/filter flow |

MockMvc standalone tests do not initialize JPA or run migrations. The integration suite fills that gap using PostgreSQL rather than an in-memory substitute. `*Test` runs in Surefire during `mvn test`; `*IT` runs in Failsafe during `mvn verify`. Integration tests are compiled during the fast build but require Docker only when executed.

The smoke test is an HTTP check, not a browser interaction test. It does not establish that Angular forms work in a real browser or that the interface passes an accessibility audit.

## Commands

See the root README for local and container commands. CI runs Maven `verify`, Angular tests and production build, then a complete Compose build/start/smoke job. Failure logs and backend reports are retained by the workflow.

For a focused backend test:

```powershell
mvn -f backend/pom.xml -Dtest=RequestServiceTest test
```

## Add tests with each feature

Authentication should include unauthorized and forbidden cases. Assignment should verify staff constraints. Comments/history should verify persistence and ordering. Version preconditions should test stale-client conflicts. Add Playwright journeys for submit → filter → open → change status and keyboard navigation before publishing the demo.

JaCoCo currently reports the fast backend suite; it does not merge Failsafe integration coverage into that report. No arbitrary coverage percentage is enforced. Review uncovered business branches and add tests where they protect a meaningful behavior.
