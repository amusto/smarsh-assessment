# Pattern: Cache-aside read-through

> **Status:** Implemented

**Related ADRs:**
- [ADR-0007 — Cache-aside read-through orchestration](../adr/0007-cache-aside-read-through.md)
- [ADR-0004 — Last-modified as the original fetch date](../adr/0004-last-modified-as-fetch-date.md)
- [ADR-0003 — SHA-256 cache filename](../adr/0003-file-cache-sha256-filename.md)

**Implementation:**
- `src/main/java/com/example/urlcache/service/UrlCacheService.java`
- `src/main/java/com/example/urlcache/cache/FileCacheStore.java`

## Intent

Enforce the exercise's core rule — **fetch each URL from the web exactly
once** — in a single place, and report the *original* fetch date consistently
on every run, whether the content came from the web or the cache.

## How it works

The service asks the store whether the URL is already cached. On a hit, it
reads content from the store; on a miss, it fetches from the web, writes to
the store, then reads back the timestamp from the store. In **both** paths,
the reported date is derived from `CacheStore.fetchedDate(url)` — the file's
last-modified time.

```text
UrlCacheService.get(url):

   store.exists(url) ?
       yes ─> content = store.read(url)                       source = CACHE
       no  ─> content = fetcher.fetch(url)                    source = WEB
              store.write(url, content)

   date = store.fetchedDate(url)   <- same call in both paths
   return CachedContent(url, content, date, source)
```

This is a textbook **cache-aside** flow (application, not the store, decides
when to fill the cache) that also acts as a **read-through** because callers
only ever ask the service — never the fetcher or the store directly.

## Implementation

- **`UrlCacheService.get(String url)`** — the one method that encodes the
  rule. It logs `Cache hit` / `Cache miss` at each branch, then always calls
  `store.fetchedDate(url)` for the timestamp. The `ContentSource` enum
  (`WEB` | `CACHE`) rides along on the returned `CachedContent` record so the
  caller can display provenance.
- **`FileCacheStore.fetchedDate(String url)`** — implements the date lookup
  as `Files.getLastModifiedTime(pathFor(url)).toInstant()`, keyed by
  `sha256(url) + ".txt"`.
- **`FileCacheStore.write(String url, String content)`** — creates the cache
  directory if needed and writes the content in UTF-8. The write happens
  **after** a successful fetch, so a failed fetch leaves the cache untouched.

Two unit tests in `UrlCacheServiceTest` prove the rule directly:

- `cacheHit_readsFromFileAndDoesNotFetch` asserts
  `verify(fetcher, never()).fetch(anyString())` on the hit path.
- `cacheMiss_fetchesFromWebAndSaves` asserts `verify(store).write(URL, content)`
  on the miss path.

## Security considerations

- **Filename safety:** the URL is not used as a filename. `FileCacheStore`
  hashes the URL with SHA-256 (see [ADR-0003](../adr/0003-file-cache-sha256-filename.md))
  so path-traversal characters, illegal characters, and length limits are all
  neutralized before the filesystem is touched.
- **Failure isolation:** because the write happens only after a successful
  fetch, a partial or corrupt cache entry is not produced when the network
  fails. A failed fetch surfaces as a `RemoteFetchException` (see the
  [exception-hierarchy](delivery-agnostic-exceptions.md) pattern) and the
  cache file is never created.
- **No TTL / no revalidation** — this is a deliberate scope choice
  ([ADR-0007](../adr/0007-cache-aside-read-through.md)), matching the exercise's
  "fetch only once" requirement. A production cache would add eviction and
  revalidation.

## Interview / review talking points

- The rule "fetch only once" lives in *one* method — trivial to read, trivial
  to test.
- Deriving the date from the store in both paths eliminates a common bug:
  reporting `Instant.now()` on a cache hit and drifting from the original.
- The distinction between cache-aside and read-through matters here: the
  service orchestrates the fill (cache-aside), and callers only ever talk to
  the service (read-through). Adapters do not know the store exists.
- The behavior is enforced by mocked unit tests (`Mockito.verify(...never())`),
  not just by inspection — regressions to the rule fail CI.
