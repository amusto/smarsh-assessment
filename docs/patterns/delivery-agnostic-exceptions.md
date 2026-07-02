# Pattern: Delivery-agnostic exception hierarchy

> **Status:** Implemented

**Related ADR:** [ADR-0008 — Handle fetch failures at the adapter boundary](../adr/0008-graceful-failure-at-boundary.md)

**Implementation:**
- Base type: `src/main/java/com/example/urlcache/common/UrlCacheException.java`
- Storage domain: `common/CacheException.java`, `common/CacheReadException.java`, `common/CacheWriteException.java`
- Remote domain: `common/RemoteFetchException.java`, `common/InvalidUrlException.java`
- Boundary handler: `src/main/java/com/example/urlcache/CacheRunner.java`

## Intent

Give failures a **domain shape** rich enough for any boundary to act on, and
keep them free of transport concerns (HTTP status codes, exit codes, Kafka
retry semantics). The core signals *what went wrong*; the adapter decides
*what "handled" means* in its context.

## How it works

There is one abstract base, `UrlCacheException`, extending `RuntimeException`.
It carries two semantic hooks:

- `String userMessage()` — safe, client-facing text. Never leaks internals.
- `boolean isRetryable()` — a hint each boundary can act on (e.g. a Kafka
  adapter would retry vs. dead-letter; a web adapter might map to 503 vs. 400).

Concrete subtypes live in two domain branches:

```text
UrlCacheException  (abstract, RuntimeException)
    |
    +-- CacheException                 "A local cache error occurred."
    |     +-- CacheReadException
    |     +-- CacheWriteException
    |
    +-- RemoteFetchException           "The remote content could not be retrieved."
          +-- InvalidUrlException      "The URL provided is not valid."
```

The boundary catches the **base type** — `UrlCacheException` — so any current
or future subtype is handled without changing the boundary code
(Open/Closed).

```text
CacheRunner.run(...):
    try { cacheService.get(url); ... }
    catch (UrlCacheException e) {
        log.error("Could not retrieve content for {}: {}", url, e.userMessage());
    }
```

## Implementation

- **`UrlCacheException`** — abstract base with default `userMessage()` and
  `isRetryable() = false`. Every subtype either accepts the default or
  overrides.
- **`RemoteFetchException`** — overrides `isRetryable() = true` (transient
  network failures are worth another attempt in a retry-capable boundary).
  Thrown by `HttpWebContentFetcher.fetch` on non-200 responses or I/O errors.
- **`InvalidUrlException`** — extends `RemoteFetchException` but overrides
  `isRetryable() = false` (a malformed URL will not become well-formed on
  retry). Thrown by `HttpWebContentFetcher.fetch` when `URI.create(url)`
  rejects the input, *before* any network call.
- **`CacheException` / `CacheReadException` / `CacheWriteException`** —
  storage-domain failures thrown by `FileCacheStore`. `AccessDeniedException`
  (a subtype of `IOException`) is captured under `CacheWriteException`.
- **`CacheRunner.run`** — the single catch block for the CLI adapter. Logs
  the safe user-facing message; the process exits normally
  (see [ADR-0008](../adr/0008-graceful-failure-at-boundary.md) and the
  README "Assumptions" #9 for the exit-code rationale).

The `CacheRunnerTest.run_whenFetchFails_handlesGracefullyWithoutCrashing` test
pins the boundary contract: any `UrlCacheException` subtype is caught
without escaping the runner.

## Security considerations

- **No stack-trace leakage:** the boundary logs `e.userMessage()`, not
  `e.getMessage()` or the full stack — internals never travel outward.
- **Safe default:** `UrlCacheException.userMessage()` returns a generic
  message; subtypes opt in to more specific (but still safe) text. A new
  subtype cannot accidentally leak a raw path or query by omission.
- **Retry semantics are declared, not assumed:** `isRetryable()` prevents an
  adapter from silently retrying a permanent failure (like a bad URL) and
  wasting resources — or worse, hammering an upstream during an outage.

## Interview / review talking points

- The base type is the boundary contract. New failure modes are additive:
  add a subtype, and the existing `catch (UrlCacheException e)` handles it.
  Open/Closed by construction.
- `userMessage()` and `isRetryable()` are *transport-agnostic* semantics.
  The same hierarchy can serve a CLI, an HTTP endpoint, or a Kafka consumer
  — each maps the semantics to its own response.
- Splitting `InvalidUrlException` off from `RemoteFetchException` while
  keeping the inheritance says: "this is a fetch-domain failure, but a
  non-retryable one" — the type hierarchy encodes the semantic.
- The design is deliberately unchecked (`RuntimeException`) so business
  code stays readable; the *type* is what carries the meaning, not a
  `throws` clause every method has to declare.
