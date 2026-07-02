# Pattern: Web adapter (second driving adapter)

> **Status: Proposed — not yet implemented.**
>
> This pattern describes a decision the codebase is *designed* to accept but
> does not currently exhibit. The seams — the service, the ports, the
> exception hierarchy, the correlation-ID scaffold — are all in place; the
> adapter itself has not been built. Tracked as Phase 11 in the
> [ROADMAP](../../ROADMAP.md) ("Web interface (stretch)").

**Related ADR(s):** Extends
[ADR-0001 — Layered, delivery-agnostic core](../adr/0001-layered-delivery-agnostic-core.md),
[ADR-0006 — Per-run correlation ID](../adr/0006-per-run-correlation-id.md),
and [ADR-0008 — Graceful failure at boundary](../adr/0008-graceful-failure-at-boundary.md).
A new ADR would be added at the point of implementation.

**Implementation:** none yet. When built, it would live beside the existing
CLI runner, activated behind a Spring profile so the CLI stays spec-compliant.

## Intent

Prove — and eventually demonstrate — that the design absorbs a new delivery
style additively: a second driving adapter (HTTP) reads the same
`UrlCacheService` and reuses the same exception hierarchy and correlation-ID
concept, without any changes to the core.

## How it would work

Three additive pieces, each a natural extension of an existing pattern:

```text
    HTTP client
        |
        v
   +---------------------------+
   |  ContentController        |  driving adapter (web) — @RestController
   |  (behind "web" profile)   |
   +-------------+-------------+
                 |
                 v
   +---------------------------+
   |  UrlCacheService          |  unchanged core
   +---------------------------+

   plus:
    CorrelationIdFilter        — OncePerRequestFilter, MDC key "requestId"
    ApiExceptionHandler        — @RestControllerAdvice mapping UrlCacheException subtypes
```

1. **`ContentController`** — a `@RestController` behind a Spring profile
   (`@Profile("web")`) with a read-only endpoint (e.g. `GET /api/content?url=...`)
   that returns a DTO derived from `CachedContent`. Depends only on the
   existing `UrlCacheService`. No entity leakage (returns a response record,
   not the internal `CachedContent`).
2. **`CorrelationIdFilter`** — an `OncePerRequestFilter` that reads
   `X-Correlation-Id` from the request or generates a UUID, writes it to MDC
   under `requestId`, and echoes it on the response header. This is the
   per-*request* companion the current per-run pattern was explicitly designed
   to layer under (see the last consequence in
   [ADR-0006](../adr/0006-per-run-correlation-id.md) and the
   [correlation-ID pattern](correlation-id-traceability.md)).
3. **`ApiExceptionHandler`** — a `@RestControllerAdvice` that catches
   `UrlCacheException` (the base type, per the
   [exception-hierarchy](delivery-agnostic-exceptions.md) pattern) and maps
   it to HTTP: `InvalidUrlException` → 400, `RemoteFetchException` → 502,
   `CacheException` → 500. The response body is a small `ApiError` DTO that
   includes the `requestId` from MDC and the exception's `userMessage()` —
   never `getMessage()` or a stack trace. `isRetryable()` can flip the
   `Retry-After` header on retryable failures.

Because `UrlCacheException` already carries the transport-agnostic semantics
(`userMessage`, `isRetryable`), the mapping in step 3 is short — it names
subtypes, sets statuses, and is done. No new "web exceptions" are needed.

## Implementation (when built)

- **New module dependency:** `spring-boot-starter-web`, added behind the same
  `web` profile so the default CLI build stays minimal.
- **Profile activation:** `@Profile("web")` on the controller, filter, and
  advice — invoked with `--spring.profiles.active=web`. The `CacheRunner`
  keeps `@Profile("!web")` (or an equivalent guard) so a single process is
  never both CLI and web.
- **No changes to the core.** `UrlCacheService`, `WebContentFetcher`,
  `CacheStore`, or any exception in `common/` remain untouched. The
  [ports-and-adapters](ports-and-adapters.md) pattern is the proof of that
  invariant.

## Security considerations

- **Endpoint scoped read-only.** The endpoint reads from the cache; it does
  not accept user-supplied content to write. Any control over which URLs may
  be fetched (allowlist? auth?) is an adapter-level policy, not a core one.
- **Correlation ID trust.** The filter should treat any incoming
  `X-Correlation-Id` as untrusted — cap its length, restrict character set,
  or overwrite it entirely — because it will appear verbatim in logs.
- **Error responses never leak internals.** Because the mapping uses
  `userMessage()`, not `getMessage()`, and never a stack trace, adding the
  HTTP surface does not weaken the [exception hierarchy's](delivery-agnostic-exceptions.md)
  containment.
- **Authentication / authorization** is out of scope for this pattern; if
  added, it lives in the adapter (Spring Security config) and is not a core
  concern.

## Interview / review talking points

- The value of the current design is measured by how *small* this pattern
  needs to be. The exception hierarchy already gives the advice its
  semantics; the correlation-ID scaffold already gives the filter a home;
  the service is already delivery-agnostic. The web adapter is three files
  and one dependency.
- The pattern is intentionally documented as **Proposed** rather than
  invented ahead of time. The `web` profile boundary is what keeps this
  option open without paying its cost today (Spring Boot's dependency
  surface stays minimal for the CLI).
- Adding this adapter is exactly the change [ADR-0001](../adr/0001-layered-delivery-agnostic-core.md)
  was written to accommodate — its consequences ("new entry points are
  additive adapters, not rewrites") predict the diff.
