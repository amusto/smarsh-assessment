# Pattern: Correlation-ID traceability (per-run)

> **Status:** Implemented (per-run). Per-*request* correlation is
> forward-looking — see the [web-adapter](web-adapter.md) pattern.

**Related ADR:** [ADR-0006 — Per-run correlation ID for traceability](../adr/0006-per-run-correlation-id.md)

**Implementation:**
- `src/main/java/com/example/urlcache/CacheRunner.java`
- `src/main/resources/application.properties`

## Intent

Give every log line from a single execution of the program a shared identifier
so one run's trail can be isolated in aggregated logs. In a request/response
service the unit of correlation is the request; in a one-shot CLI it is the
**run**.

## How it works

At the start of each run, `CacheRunner` generates a UUID and puts it into
SLF4J's **MDC** under the key `runId`. The Logback pattern
(configured via `logging.pattern.level`) surfaces the MDC value on every log
line. A `finally` block clears the MDC so nothing leaks across boundaries
(important when tests or future adapters share a thread).

```text
CacheRunner.run(...):
    MDC.put("runId", UUID.randomUUID().toString())
    try {
        ... cacheService.get(url) ...            <- every log line inside carries runId
    } catch (UrlCacheException e) { ... }
    finally {
        MDC.remove("runId")
    }
```

Log output looks like:

```text
 INFO [runId=8a1c7c26-...] c.e.u.s.UrlCacheService : Cache miss for https://example.com - fetching from web
 INFO [runId=8a1c7c26-...] c.e.u.s.UrlCacheService : Saved content for https://example.com to cache
```

## Implementation

- **`CacheRunner`** — declares `private static final String RUN_ID = "runId"`,
  calls `MDC.put(RUN_ID, UUID.randomUUID().toString())` at the top of `run(...)`,
  and `MDC.remove(RUN_ID)` in `finally`. No other class touches the MDC.
- **`application.properties`** —
  `logging.pattern.level=%5p [runId=%X{runId:-}]`. The `%X{runId:-}` prints the
  MDC value, falling back to empty if unset (so tests without MDC don't crash
  the format).
- **`CachedContent.source`** (`WEB` | `CACHE`) — a complementary piece of
  provenance that rides on the returned record rather than the log line.
  Together they answer: which run produced this? did it hit the cache or the
  network?

## Security considerations

- The correlation ID is a random UUID, not derived from any user input, so it
  cannot be forged into an ID that collides with a prior run or exfiltrates a
  secret.
- `MDC.remove` in `finally` prevents cross-contamination — critical if the
  same thread runs multiple units of work (e.g. a future scheduler or a web
  adapter reusing the thread pool).
- No sensitive payload is placed in MDC. Only the run ID lives there; the URL
  and content stay on the log message body and program output.

## Interview / review talking points

- The correlation-ID *idea* is universal, but the correlation *unit* depends
  on the delivery style. Applying "per-request" verbatim to a CLI would be a
  category error; adapting to "per-run" is the honest translation.
- MDC + `finally` is a two-line pattern with high leverage: aggregate logs
  become greppable by `runId=<id>` without any framework beyond SLF4J.
- The pattern is already primed for a web adapter: an
  `OncePerRequestFilter` that reads or generates an `X-Correlation-Id` and
  writes it to MDC layers cleanly on top (see the
  [web-adapter](web-adapter.md) forward-looking pattern).
