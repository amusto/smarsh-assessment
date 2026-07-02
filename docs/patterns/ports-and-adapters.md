# Pattern: Ports & Adapters (delivery-agnostic core)

> **Status:** Implemented

**Related ADR:** [ADR-0001 — Layered, delivery-agnostic core](../adr/0001-layered-delivery-agnostic-core.md)
**Implementation:**
- Driving adapter: `src/main/java/com/example/urlcache/CacheRunner.java`
- Core: `src/main/java/com/example/urlcache/service/UrlCacheService.java`
- Driven ports: `src/main/java/com/example/urlcache/fetch/WebContentFetcher.java`, `src/main/java/com/example/urlcache/cache/CacheStore.java`
- Driven adapters: `src/main/java/com/example/urlcache/fetch/HttpWebContentFetcher.java`, `src/main/java/com/example/urlcache/cache/FileCacheStore.java`

## Intent

Keep the cache-orchestration logic independent of *how it is invoked* (CLI vs.
web vs. batch) and *how it reaches the outside world* (JDK HTTP client, file
system, or a future replacement). Requirements about delivery and storage are
absorbed at the edges without rewriting the middle.

## How it works

The service defines two interfaces — the **ports** — and depends only on those.
The CLI runner **drives** the service from one side; a web fetcher and a file
store **are driven by** it on the other. Concrete classes are wired by Spring's
constructor injection.

```text
   +---------------------------+
   |   CacheRunner (CLI)       |  driving adapter (@Component,
   |   CommandLineRunner       |     implements CommandLineRunner)
   +-------------+-------------+
                 |
                 v
   +---------------------------+
   |   UrlCacheService         |  core (@Service) — depends only on ports
   +----+-----------------+----+
        |                 |
        v                 v
+---------------+  +----------------+
| WebContent    |  |  CacheStore    |   driven ports (interfaces)
|   Fetcher     |  |                |
+-------+-------+  +--------+-------+
        ^                   ^
        |                   |
+---------------+  +----------------+
| HttpWebContent|  | FileCacheStore |   driven adapters
|   Fetcher     |  |                |
+---------------+  +----------------+
```

The runner takes one URL from configuration (`@Value("${app.url:}")`), calls
`cacheService.get(url)`, prints the result, and lets the process exit. Nothing
about "we're a CLI" leaks into `UrlCacheService`. Nothing about "we use the
JDK HTTP client" or "we store as files" leaks either — those live behind the
interfaces.

## Implementation

- **`CacheRunner`** — the only class that touches `System.out`, MDC, `@Value`,
  and `CommandLineRunner`. It reads config, invokes the service, prints, and
  handles boundary concerns (see the [exception-hierarchy](delivery-agnostic-exceptions.md)
  and [correlation-ID](correlation-id-traceability.md) patterns).
- **`UrlCacheService.get(url)`** — the entire cache-aside rule lives here
  (see [cache-aside](cache-aside.md)). It depends on `WebContentFetcher` and
  `CacheStore` interfaces, nothing else.
- **`WebContentFetcher`** — one method: `String fetch(String url)`. Throws
  domain exceptions on failure; never surfaces the transport library.
- **`CacheStore`** — four methods (`exists`, `read`, `write`, `fetchedDate`)
  keyed by URL. The store speaks in URLs; hashing is an internal detail.
- **`HttpWebContentFetcher`** — JDK `java.net.http.HttpClient` implementation
  (see [ADR-0002](../adr/0002-use-jdk-httpclient.md)).
- **`FileCacheStore`** — file-backed store with SHA-256 filenames
  (see [ADR-0003](../adr/0003-file-cache-sha256-filename.md)).

Swapping either adapter is a `@Component` swap; the core does not change.

## Security considerations

- The core cannot accidentally couple itself to a transport-specific concern
  (HTTP status codes, servlet request objects) because those types are not on
  its classpath by design.
- Adapters are the only place where untrusted input crosses the boundary.
  Keeping them thin means each is easy to audit (e.g. URL parsing in
  `HttpWebContentFetcher.fetch` throws `InvalidUrlException` before any
  network call).

## Interview / review talking points

- The interfaces are placed exactly where requirements tend to change — the
  fetch source and the storage mechanism — so future change is additive.
- A second driving adapter (web endpoint) is a new `@Component` beside the
  runner, not a rewrite. See the [web-adapter](web-adapter.md) forward-looking
  pattern.
- The dependency direction points *inward*: adapters depend on the core; the
  core depends only on ports it defines itself. This is why the exception
  hierarchy is delivery-agnostic (`userMessage`/`isRetryable` rather than HTTP
  status codes).
- The trade-off is a few more small files vs. one `main` method — but each is
  single-purpose, and the seams pay for themselves the first time a
  requirement changes.
