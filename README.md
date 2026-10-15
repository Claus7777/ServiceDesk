# Service Desk

A full-stack service-request tracker for small teams. Submit a request, find it by title or status, and move it from **New** to **In progress** to **Resolved**.

Built as a junior developer portfolio project with explainable decisions, meaningful tests, and small daily milestones.

## Stack

| Layer | Technology |
| --- | --- |
| API | Java 21, Spring Boot 4.0.8, Spring MVC, Bean Validation |
| Persistence | PostgreSQL 17, Spring Data JPA, Flyway SQL migrations |
| Web | Angular 21.2.24, TypeScript 5.9, standalone components, reactive forms |
| Tests | JUnit Jupiter 6, Mockito, MockMvc, Testcontainers, Angular/Vitest |
| Delivery | Docker Compose, multi-stage Docker builds, Nginx, GitHub Actions |

Angular 21 is an LTS choice; see [Angular compatibility](https://angular.dev/reference/versions). Spring Boot manages the JUnit and Mockito versions; see [Spring Boot documentation](https://docs.spring.io/spring-boot/4.0/).

## Run with Docker

Prerequisite: Docker Engine/Desktop with the Compose plugin and Linux containers enabled. No host Java, Node, or PostgreSQL installation is required for this path.

From this repository's root in PowerShell:

```powershell
Copy-Item .env.example .env
docker compose up --build --wait
```

Open **http://localhost:4200**. Create a request, search for it, open its details by clicking its title, and change its status.

The first build downloads images and dependencies. PostgreSQL data lives in a named Docker volume. Only the frontend is published, on the loopback interface. Nginx forwards `/api/` to Java over the Compose network.

```powershell
docker compose logs -f backend
docker compose down
```

Stopping the stack preserves the database. `docker compose down --volumes` deletes its stored data; use it only when intentionally resetting your local demo.

This first milestone is a **local shared demo** without authentication. Anyone with access to its API can read and update requests. Add authentication and authorization before public deployment or real customer data. `.env.example` contains development credentials only; keep `.env` out of Git.

## Test

Run the fast suites entirely in containers:

```powershell
docker compose --profile test run --rm backend-tests
docker compose --profile test run --build --rm frontend-tests
```

Run all backend tests, including real PostgreSQL integration tests, with Java 21+, Maven 3.9+, and Docker available on the host:

```powershell
mvn -B -f backend/pom.xml verify
```

`mvn test` runs JUnit/Mockito and MockMvc tests without a database. `mvn verify` additionally runs `*IT` tests with Testcontainers. Integration tests deliberately fail if Docker is unavailable; they do not silently skip. Coverage: `backend/target/site/jacoco/index.html` (fast suite). Reports: `backend/target/surefire-reports` and `backend/target/failsafe-reports`.

Frontend locally, with Node 24 and pnpm 11.19.0:

```powershell
cd frontend
pnpm install --frozen-lockfile
pnpm test
pnpm build
```

To exercise the full container stack after startup, with Python 3 available:

```powershell
python scripts/smoke.py
```

This creates one sample request and leaves it resolved in the database.

## Develop locally

Start PostgreSQL only:

```powershell
docker compose -f compose.yaml -f compose.dev.yaml up -d database
```

In separate terminals:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

```powershell
cd frontend
pnpm install --frozen-lockfile
pnpm start
```

The Angular development server proxies `/api/**` to `localhost:8080`. The API defaults match `.env.example`; custom values must be supplied as `DB_URL`, `DB_USER`, and `DB_PASSWORD` environment variables when Java runs on the host. Compose `.env` is not automatically read by Spring Boot.

## Windows Docker troubleshooting

Check both the client and engine before starting the stack:

```powershell
docker version
docker compose version
wsl --version
```

`docker version` must show a **Server** section. A working CLI/Compose version alone does not mean the Linux engine is running. Start Docker Desktop and wait for the engine to become ready.

If `docker` is missing from an existing terminal after installation, open a new terminal. For a per-user Docker Desktop installation, the CLI may be at `$env:LOCALAPPDATA\Programs\DockerDesktop\resources\bin\docker.exe` instead of under Program Files.

If Windows reports WSL is not installed, Docker's WSL backend cannot start. Installing WSL is a separate administrator-level Windows setup step and may require a restart. With the machine owner's approval, run this in an administrator PowerShell window:

```powershell
wsl --install --no-distribution
```

Restart Windows if requested, open Docker Desktop again, then verify `wsl --version` and the Server section of `docker version`. Docker Desktop does not require an additional user Linux distribution for this project. See [Docker's WSL backend requirements](https://docs.docker.com/desktop/features/wsl/) and [Microsoft's WSL installation options](https://learn.microsoft.com/en-us/windows/wsl/basic-commands).

Compose configuration can be validated without a running engine, but this does not build images or run tests:

```powershell
docker compose config --quiet
docker compose -f compose.yaml -f compose.dev.yaml config --quiet
docker compose --profile test config --quiet
```

## Implemented

- Create requests with title, description, and priority.
- Paginated listing with status filtering and literal, case-insensitive title search.
- Request details and status changes, including reopening resolved requests.
- Server and client validation, loading/empty/error states, duplicate-submit protection.
- UTC timestamps, optimistic locking, versioned SQL schema.
- Container definitions, health checks, local persistence, CI configuration.

## Next milestones

Authentication and staff permissions → assignment and comments → append-only status history → accessibility and browser tests → user feedback → public demo.

See [the daily roadmap](docs/roadmap.md) for small deliverables and acceptance checks. CI is configured to run backend integration tests, frontend tests/build, and a Compose smoke test when pushed to GitHub.

## Documentation

- [Architecture and decisions](docs/architecture.md)
- [API contract and examples](docs/api.md)
- [Testing strategy](docs/testing.md)
- [Daily roadmap](docs/roadmap.md)
- [Development journal](docs/devlog.md)
- [Contribution workflow](CONTRIBUTING.md)

## Verification status

Verified locally on 2026-10-15 with Docker Desktop 4.95.0: production images build, all three application containers are healthy, 13 backend tests (including real PostgreSQL integration tests) pass, all 6 Angular tests pass inside Docker, and the HTTP smoke check passes through Nginx. The backend suite ran on host Java 22 targeting Java 21; the deployed API runs on Java 21 in Docker.

See [the development journal](docs/devlog.md) for the commands, results, and historical setup troubleshooting. GitHub Actions and full browser interaction/accessibility testing remain unverified. A configured workflow is not evidence of a passing GitHub run.
