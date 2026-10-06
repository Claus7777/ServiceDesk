# Development workflow

1. Pick one issue with a user-facing goal and acceptance checks.
2. Create a branch such as `codex/request-comments`.
3. Make the smallest coherent change. Add tests for the relevant behavior and failure case.
4. Update the API contract, migration, architecture notes, or journal when the change affects them.
5. Run fast tests/build; run `mvn verify` and container checks for persistence or delivery changes.
6. Open a pull request explaining the problem, changed behavior, and checks actually run.

Useful commit messages: `feat: add staff assignment`, `fix: retain request draft after failed save`, `test: cover stale status updates`, `docs: explain login authorization decisions`.

Never commit `.env`, credentials, generated builds, dependency caches, or customer data. Add a new Flyway migration for schema changes; never rewrite an applied migration. Mock dependencies at a meaningful boundary and use PostgreSQL integration tests when correctness depends on the database.
