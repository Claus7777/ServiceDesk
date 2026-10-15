# Development journal

## 2026-10-15 — Container verification passed after Docker update

**Environment:** Docker Desktop 4.95.0, Linux engine 29.8.2, Compose v5.6.0. The engine is reachable following the user's Desktop update; the earlier runtime socket startup failure no longer blocks this project.

**Checks completed:**

- `docker compose up --build --wait --wait-timeout 240`: exit 0. Backend and frontend production images built; PostgreSQL, Java API, and Nginx frontend all reported healthy.
- `mvn -B -f backend/pom.xml verify` (using the project-local Maven dependency cache): exit 0. All 11 fast backend tests and both Testcontainers PostgreSQL integration tests passed, with no failures/errors/skips. Host Java is 22.0.1 with compilation targeting 21; the deployed API runs on Java 21 in Docker.
- `docker compose --profile test run --build --rm frontend-tests`: exit 0. All 6 Angular/Vitest tests passed in the Linux test container.
- `python scripts/smoke.py`: exit 0. Nginx served the frontend; the proxied API created, read, updated, persisted, and filtered a request against the Compose PostgreSQL database.

**Result:** first milestone's container/runtime verification is complete. No application code changes were required. The stack is left running at http://localhost:4200 with one resolved smoke-test request in its persistent database. Earlier failure entries below are historical.

**Remaining limits:** GitHub Actions has not been executed as part of this local session. The smoke test is HTTP-based; browser interaction/accessibility testing and authentication remain future milestones.

## 2026-10-15 — Post-restart Docker startup check

**Verified:** Windows restarted after WSL setup; WSL now reports default version 2. The Docker CLI uses the `desktop-linux` context, but no Linux engine becomes available.

**Failure:** Docker Desktop 4.85.0 crashes during startup while attempting to remove its `Docker/run/dockerInference` Unix socket (`The file cannot be accessed by the system`). The WSL message about no user distributions is incidental; the backend's explicit fatal error identifies its runtime socket listener.

**Reversible recovery attempted:** stopped Desktop with its official `desktop stop --force` command, confirmed Docker processes were stopped, and preserved the two runtime directories as `Docker/run.backup-20261015-124430` and `docker-secrets-engine.backup-20261015-124430`. No settings, images, VM disks, or volumes were reset/deleted. On restart, Desktop recreated sockets and crashed in Secrets Engine at `docker-secrets-engine/engine.sock` with the same error. No Docker subprocess remained after the final stop check.

**Reference:** matching upstream reports: https://github.com/docker/desktop-feedback/issues/554 and https://github.com/docker/desktop-feedback/issues/531. These reports describe related failures; they do not establish that a particular upgrade will fix this machine.

**Status:** Docker Desktop recovery is still required. Image builds, container startup, PostgreSQL integration tests, and the HTTP smoke check remain unexecuted. Application code was not changed during this diagnosis.

## 2026-10-15 — Docker prerequisite diagnosis

**Goal:** verify the container stack and PostgreSQL integration tests after Docker Desktop installation.

**Verified:** found the per-user Docker Desktop installation under `AppData/Local/Programs/DockerDesktop`; Docker CLI 29.6.2 and Compose v5.3.1 execute. Base, development override, and test-profile Compose configurations all pass `config --quiet` validation.

**Blocker:** Docker Desktop was started, but the engine reports it is unable to start. Windows reports WSL is not installed. Firmware virtualization and second-level address translation are reported enabled. Docker's backend log reports no virtualization backend available; container startup and integration tests have not run.

**System setup:** automatic approval review initially rejected WSL installation pending explicit authorization. The user then approved it. Ran `wsl --install --no-distribution` with Windows administrator elevation; the installer completed with exit code 0. WSL reports version 3.0.3.0. Windows servicing confirms VirtualMachinePlatform installed successfully and **a restart is required**. No automatic restart was performed.

**Documentation:** added Windows setup diagnostics and the official Docker/Microsoft references to the README.

**Next:** the user must restart Windows and reopen Docker Desktop. Then verify engine availability and run Compose startup, PostgreSQL integration tests, and the HTTP smoke check. These runtime checks remain pending; Compose configuration validation alone does not establish runtime correctness.

## 2026-10-06 — First request workflow

**Goal:** establish a small, understandable Java/Angular/PostgreSQL application that can grow through daily portfolio milestones.

**Implemented:** request creation, paginated list with title/status filters, details, status changes; explicit Flyway schema; UTC clock injection; DTO boundaries; JPA optimistic locking; Angular reactive forms and signals; user feedback states; Docker Compose/Nginx; testing suites and GitHub Actions configuration.

**Decisions:** use one backend, a real PostgreSQL integration suite, a same-origin frontend proxy, and framework-managed JUnit Jupiter/Mockito versions. Start with a local shared demo, then implement user accounts and permissions before public deployment.

**Verification:** 11 backend JUnit/Mockito/MockMvc tests passed (zero failures/skips); the executable Spring Boot jar packaged successfully. All 6 Angular/Vitest tests passed and the production build succeeded (218.93 kB initial bundle). Java compilation targeted 21 using the available host JDK 22; Docker/CI target JDK 21. Docker is unavailable in the initial coding environment, so Compose startup, image builds, PostgreSQL integration execution, and the container smoke check remain unverified locally. CI has been defined but has not run on GitHub.

**Next:** verify the Docker stack and integration suite on a Docker-enabled machine, then design accounts and authorization with clear ownership rules.
