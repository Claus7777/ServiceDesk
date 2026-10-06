# Development journal

## 2026-10-06 — First request workflow

**Goal:** establish a small, understandable Java/Angular/PostgreSQL application that can grow through daily portfolio milestones.

**Implemented:** request creation, paginated list with title/status filters, details, status changes; explicit Flyway schema; UTC clock injection; DTO boundaries; JPA optimistic locking; Angular reactive forms and signals; user feedback states; Docker Compose/Nginx; testing suites and GitHub Actions configuration.

**Decisions:** use one backend, a real PostgreSQL integration suite, a same-origin frontend proxy, and framework-managed JUnit Jupiter/Mockito versions. Start with a local shared demo, then implement user accounts and permissions before public deployment.

**Verification:** 11 backend JUnit/Mockito/MockMvc tests passed (zero failures/skips); the executable Spring Boot jar packaged successfully. All 6 Angular/Vitest tests passed and the production build succeeded (218.93 kB initial bundle). Java compilation targeted 21 using the available host JDK 22; Docker/CI target JDK 21. Docker is unavailable in the initial coding environment, so Compose startup, image builds, PostgreSQL integration execution, and the container smoke check remain unverified locally. CI has been defined but has not run on GitHub.

**Next:** verify the Docker stack and integration suite on a Docker-enabled machine, then design accounts and authorization with clear ownership rules.
