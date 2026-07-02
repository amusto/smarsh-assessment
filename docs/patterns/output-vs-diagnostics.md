# Pattern: Program output vs. diagnostics

> **Status:** Implemented

**Related ADR:** [ADR-0005 — Separate program output from troubleshooting logging](../adr/0005-output-vs-logging.md)

**Implementation:**
- `src/main/java/com/example/urlcache/CacheRunner.java`
- `src/main/java/com/example/urlcache/service/UrlCacheService.java`

## Intent

Keep the required program output (the *result* — date, URL, content) on a
dedicated channel that a caller can rely on, and put troubleshooting detail
(cache hit vs. miss, error reasons) on a separate, leveled channel that can
be filtered independently.

## How it works

Two channels, two responsibilities:

| Channel | Content | Where |
|---|---|---|
| **Program output** — `System.out` | The result: `Original fetch date`, `URL`, `Source`, `Content` | `CacheRunner.run` (adapter only) |
| **Diagnostics** — SLF4J logger | Cache hit / miss decisions, save confirmations, boundary error lines | `UrlCacheService`, `CacheRunner` |

Every user-visible print statement lives in `CacheRunner`. The service and
adapters only log through SLF4J — never `System.out`.

## Implementation

- **`CacheRunner.run(...)`** — four `System.out.println` calls that write the
  required result. This is the only place `System.out` is touched.
- **`UrlCacheService`** — logs `Cache hit for {} - reading from local file`
  or `Cache miss for {} - fetching from web` (plus `Saved content for {}`) at
  `INFO`. These are decision-point diagnostics, not results.
- **Boundary error line** — `CacheRunner` catches `UrlCacheException` and logs
  a single `ERROR` line with the safe `userMessage()`; no stack trace, no
  `System.err.println`.
- **Level and format** — the [correlation-ID](correlation-id-traceability.md)
  pattern configures the log pattern so every diagnostic line carries a
  `[runId=<uuid>]`, allowing one run's trail to be isolated in aggregated logs.

## Security considerations

- The two channels give operators the choice of routing diagnostics somewhere
  more sensitive than stdout (a file, a log aggregator with access control),
  while the result stream stays clean for pipe-based consumers.
- Because the boundary logs `userMessage()` — not `getMessage()` or the stack
  — diagnostics never leak internal detail even at ERROR level (see the
  [exception-hierarchy](delivery-agnostic-exceptions.md) pattern).

## Interview / review talking points

- The pattern maps cleanly onto Unix convention: `stdout` is the result;
  logs (which typically flow to `stderr` or a file) are the operational
  narrative. A scripted caller can consume the result without parsing around
  log lines.
- The rule is enforced by *location* — only the adapter has permission to
  touch `System.out`. Making `UrlCacheService` unaware of stdout is what
  keeps [ports-and-adapters](ports-and-adapters.md) honest.
- Trivial in isolation, but the alternative (interleaving prints and logs in
  business code) is a common mess in real projects.
