# Daily development roadmap

Use these as work sessions, not a promise to finish each task in one day. A session can end with a failing test that demonstrates the next problem, a reviewed design, or a small completed feature. Prefer honest progress to commit streaks.

## Milestone 1 — foundation (implemented and container verification passed)

Java API, PostgreSQL migration, Angular dashboard, creation/listing/filtering/status updates, fast tests, integration test definitions, Docker and CI configuration. Read the code, run the suites, and explain the HTTP flow before extending it.

Verified on 2026-10-15: healthy Compose stack, 13 backend tests including PostgreSQL integration tests, 6 Angular tests inside Docker, and the full HTTP smoke check. See `docs/devlog.md` for evidence. CI on GitHub and browser interaction tests remain pending.

## Next 20 sessions

| Session | Deliverable | Acceptance evidence |
| --- | --- | --- |
| 1 — complete | Run Docker stack and full backend `verify` | Passed on 2026-10-15; results recorded in the journal |
| 2 | Interview one prospective user | Document their workflow and top three problems |
| 3 | Write account/role API and data design | ADR with requester/staff permissions matrix |
| 4 | Add user migration and password hashing | Persistence and password verification tests |
| 5 | Add login/logout with Spring Security | Successful and rejected-login tests |
| 6 | Protect endpoints by role/ownership | 401, 403, and cross-user access tests |
| 7 | Add Angular login and session handling | Loading, failed login, and expired-session behavior |
| 8 | Add request assignment | Only valid staff members can be assigned |
| 9 | Add assignee filters to the UI | Filter results proven by API/UI tests |
| 10 | Add comments API | Validation, authorization, persistence, ordering tests |
| 11 | Add comments UI | Empty, loading, posting, and error states |
| 12 | Persist status history in the same transaction | Request and history cannot diverge |
| 13 | Show request timeline | Clear actor/time/transition display |
| 14 | Add version or ETag preconditions | Stale browser update returns 409/412 |
| 15 | Add Playwright browser journeys | Core flow passes against Compose |
| 16 | Audit keyboard/mobile/accessibility | Document and fix concrete findings |
| 17 | Improve validation error UX | Server field errors appear by the relevant input |
| 18 | Review indexes with realistic sample data | Explain query plans before/after any optimization |
| 19 | Get user feedback and fix one observed issue | Reproduction, fix, regression test |
| 20 | Publish demo and release v0.2 | Passing CI, screenshots, demo access, release notes |

## Daily public update

```text
Today: [one concrete improvement]
Problem: [the user behavior or bug that motivated it]
Decision: [what I chose and why]
Evidence: [test output, screenshot, short clip, or PR]
Next: [one small follow-up]
```

Keep a fuller explanation in `docs/devlog.md`. Pin the repository once its README and demo are useful. Do not claim tests passed until you have run them. If using AI assistance, review the code and be prepared to explain its decisions and limitations in an interview.
