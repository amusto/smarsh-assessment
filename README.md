# url-cache

A small Java program that fetches a URL's content (page source) from the web and
caches it locally as a file. Each URL is fetched from the web **only once** — on
subsequent runs the content is served from the local file instead of the network.

## Task (from the exercise)

> Write a Java program that fetches URL content (source) from the web and saves
> it locally as a file. The program should fetch each URL from the web only once.
> If the page is already fetched (the file exists locally) the content is
> retrieved from the file, not from the web.

Each run:

1. Take a URL value.
2. Check whether its content already exists locally.
3. **If not cached:** fetch the page content from the web, save it to a file.
4. **If cached:** read the content from the file.
5. Print the **original fetch date**, the **URL**, and the **page content**.
6. The application shuts down.

## Requirements coverage

| Requirement | How it's met |
|---|---|
| Java 8 or later | Java 21 (toolchain) |
| Maven or Gradle | Gradle (Spring Boot, no web layer) |
| Unit tests in the right places | JUnit 5 + Mockito; cache hit/miss on the service, file round-trip on the store |
| Relevant prints for troubleshooting | Diagnostic logging at each decision point (separate from program output) |
| Assumptions documented | See **Assumptions** below |

## How to run

The URL is configurable (default in `application.properties`); override per run:

```bash
./gradlew bootRun --args='--app.url=https://example.com'
```

Or build a jar and run it:

```bash
./gradlew build
java -jar build/libs/url-cache-0.0.1-SNAPSHOT.jar --app.url=https://example.com
```

Run it twice with the same URL: the first run fetches from the web; the second
serves from the cache and prints the **original** fetch date.

## Use cases

### 1. First fetch (cache miss → fetched from web)

```bash
rm -rf cache/                                              # start with an empty cache
./gradlew bootRun --args='--app.url=https://example.com'
```

Logs `Cache miss ... fetching from web`, prints `Source: WEB` and a fresh date,
saves the content to `cache/<sha256>.txt`, then exits.

### 2. Repeat fetch (cache hit → served from file)

```bash
./gradlew bootRun --args='--app.url=https://example.com'
```

Logs `Cache hit ... reading from local file`, prints `Source: CACHE` and the
**same original date** as the first run — the page is never fetched twice.

### 3. Invalid or unreachable URL (handled gracefully)

```bash
./gradlew bootRun --args='--app.url=https://does-not-exist.example'
```

Fetch failures are caught at the runner boundary: a single clear error line is
logged (no stack-trace dump) and the program exits with a **non-zero exit code**
to signal failure to callers. Nothing is written to the cache.

## How to test

```bash
./gradlew test
```

Covers the cache hit/miss behavior (mocked collaborators), the file-store
round-trip (temp directory), and graceful failure handling (non-zero exit).

## Assumptions

1. **Original fetch date = the cache file's last-modified timestamp.** No
   separate metadata store is kept; the file's timestamp records when the
   content was first written. (Tradeoff noted below.)
2. **URL → filename via SHA-256 hash.** Deterministic, collision-safe, and
   filesystem-legal regardless of URL characters or length.
3. **HTTP via the JDK's built-in `java.net.http.HttpClient`** — no third-party
   HTTP library, to keep dependencies minimal.
4. **Fetch-once, no expiry.** If a cache file exists it is always used; there is
   no TTL or revalidation (matches the "fetch only once" requirement).
5. **Content is treated as UTF-8 text** (page source).
6. **Cache directory and URL are configuration**, not hardcoded constants.
7. **Program output vs. troubleshooting output are separated:** the required
   output (date, URL, content) goes to standard out; diagnostic messages go
   through a logger.
8. **One URL per run.** The design extends naturally to multiple URLs.

## Architecture

```
CommandLineRunner (CLI adapter)
        │
        ▼
   UrlCacheService          <- core orchestration (cache hit / miss)
     |-- WebContentFetcher  <- interface (web source)
     |-- CacheStore         <- interface (local storage + fetch date)
```

The core service is **delivery-agnostic**: the CLI runner is a thin adapter that
invokes the service and lets the app exit (satisfying "the app should be shut
down"). The same service could be driven by an alternative adapter (e.g. a web
endpoint) without changing the core — see the optional enhancement below.

### Designed for changing requirements

This is a deliberate emphasis of the exercise:

- **Interfaces at the boundaries** (`WebContentFetcher`, `CacheStore`) let the
  fetch source or storage mechanism change without touching the core logic.
- **Configuration over hardcoding** (URL, cache directory) absorbs the most
  common kind of change.
- **Delivery decoupled from core**, so a new entry point (web, batch, scheduled)
  is an additive adapter, not a rewrite.

Key decisions are recorded as ADRs under `docs/adr/` (rendered with Docsify).

## Optional enhancement (not part of the core requirement)

A thin **web interface** could expose the cached content for browsing (e.g.
`GET /api/content?url=...`) as a second adapter over the same `UrlCacheService`.
This is intentionally kept separate from the spec-compliant CLI, which fetches,
prints, and shuts down. Tracked in the roadmap as a stretch goal.

## Tradeoffs

- **Last-modified as the fetch date** is simple and needs no extra storage, but
  it couples the timestamp to filesystem metadata; a sidecar metadata file would
  be more explicit and portable.
- **One file per URL** is simple with no eviction policy; a production cache
  would add size limits / eviction.
- **No TTL** matches the requirement but means stale content is never refreshed;
  a real cache would support revalidation.

## Project structure

```
src/main/java/com/example/urlcache/
  UrlCacheApplication.java        # Spring Boot entry point
  CacheRunner.java                # CommandLineRunner CLI adapter + prints
  model/CachedContent.java        # url, content, fetchedDate, source (WEB|CACHE)
  fetch/WebContentFetcher.java    # interface
  fetch/HttpWebContentFetcher.java
  cache/CacheStore.java           # interface
  cache/FileCacheStore.java
  service/UrlCacheService.java    # cache hit/miss orchestration
```
