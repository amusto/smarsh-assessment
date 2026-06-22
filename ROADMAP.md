# Roadmap

Build sequence for the url-cache exercise. Phases are units of work, not calendar
time. Update the status as we progress. Each phase maps to roughly one tight,
self-contained commit so the history tells the development story.

## Status legend

| ✅ Done | 🚧 In progress | ⏭️ Next | ⬜ Not started | 🎯 Stretch |
|---|---|---|---|---|

## Progress

| # | Phase | Commit | Status |
|---|---|---|---|
| 0 | Scaffold | `chore: scaffold gradle spring boot CLI project (no web)` | ✅ |
| 1 | Docs + assumptions | `chore: add spring boot scaffold and planning docs` | ✅ |
| 2 | Domain model + interfaces | `feat: add CachedContent model and fetcher/store interfaces` | ✅ |
| 3 | Web fetcher | `feat: implement HttpWebContentFetcher` | ✅ |
| 4 | File cache store | `feat: implement FileCacheStore (read/write/exists/date)` | ✅ |
| 5 | Cache service | `feat: add UrlCacheService cache-hit/miss orchestration` | ✅ |
| 6 | CLI adapter + output | `feat: wire CommandLineRunner, config, and prints` | ✅ |
| 7 | Tests | `test: cover service hit/miss and file store round-trip` | ✅ |
| 8 | Exception handling + logging | `feat: add error handling and troubleshooting logging` | ✅ |
| 9 | Traceability (run correlation ID) | `feat: add per-run correlation ID (runId) to MDC and logs` | ⬜ |
| 10 | ADRs + Docsify | `docs: finalize ADRs and add Docsify rendering` | ⬜ |
| 11 | Web interface (stretch) | `feat: add optional web adapter over UrlCacheService` | 🎯 |

---

## Phase detail

### Phase 0 — Scaffold 🚧
Gradle + Spring Boot (no web), Java 21, clean `./gradlew build`.
**Done when:** project builds green; `.gitignore` covers `.DS_Store`, `cache/`, `logs/`.

### Phase 1 — Docs + assumptions ⬜
README (task, run, test, assumptions, architecture) + this roadmap + ADR stubs.
**Done when:** assumptions are written down before logic (requirement of the exercise).

### Phase 2 — Domain model + interfaces ⬜
`CachedContent` (url, content, fetchedDate, source) and the `WebContentFetcher`
and `CacheStore` **interfaces** — the seams that absorb changing requirements.
**Done when:** contracts compile; no implementations yet.

### Phase 3 — Web fetcher ⬜
`HttpWebContentFetcher` using the JDK `HttpClient`. Fetch page source as UTF-8.
**Done when:** can fetch a URL's content; network errors surface clearly.

### Phase 4 — File cache store ⬜
`FileCacheStore` over `java.nio.file.Files`: `exists`, `read`, `write`, and
`fetchedDate` (file last-modified). URL → SHA-256 filename.
**Done when:** content round-trips to disk; date derives from the file.

### Phase 5 — Cache service 🚧
`UrlCacheService.get(url)`: cache hit → read from store; miss → fetch, save, then
read date from the store so both paths derive the date identically.
**Done when:** the "fetch only once" rule is implemented in one place.

### Phase 6 — CLI adapter + output 🚧
`CommandLineRunner` invokes the service, prints **date / URL / content** to
stdout, then the app exits. URL + cache dir from config (`@Value`/properties).
**Done when:** `./gradlew bootRun --args='--app.url=...'` works end to end.

### Phase 7 — Tests ⬜
`UrlCacheServiceTest` (mock fetcher + store): cache hit → `verify(fetcher, never())`;
miss → `verify(fetcher)` + `verify(store).write(...)`. `FileCacheStoreTest` with a
temp directory.
**Done when:** `./gradlew test` green; the cache-once behavior is proven.

### Phase 8 — Exception handling + logging ⬜
Custom exception(s) for fetch/IO failures; handle at the runner boundary with a
clear message and non-zero exit. Troubleshooting logs at each decision point.
**Done when:** failures are graceful and traceable, not stack-trace dumps.

### Phase 9 — Traceability (run correlation ID) ⬜
Apply the correlation-ID concept to a CLI context: the unit of work is a **run**,
not an HTTP request. Generate a `runId` (UUID) at startup, store it in **MDC**,
and surface it in the log pattern (`[runId=...]`) so every troubleshooting line
from one execution shares an ID and a single run's trail can be isolated (e.g.
`grep "runId=<id>"`). Complements the `source` flag (WEB|CACHE) provenance from
the model.
**Done when:** every log line in a run carries the same `runId`; documented in an
ADR ("traceability in a CLI: per-run, not per-request").
**Note:** the per-*request* correlation filter only returns if the optional web
adapter (Phase 11) is built.

### Phase 10 — ADRs + Docsify ⬜
Finalize ADRs (last-modified-as-date, URL hashing, built-in HttpClient,
program-output-vs-logging, per-run traceability) and add a Docsify `index.html`
+ `_sidebar.md`.
**Done when:** ADRs render as a small docs site.

### Phase 11 — Web interface (stretch) 🎯
Optional second adapter: `spring-boot-starter-web` + a read-only endpoint over
the same `UrlCacheService`, behind a `web` profile so the CLI stays spec-compliant.
If built, this is where a per-request correlation-ID filter would return.
**Done when:** only attempted after Phases 0–9 are complete and green.
