# ADR-0007: Cache-aside read-through orchestration

Status: Accepted

## Context

The "fetch only once" rule has two paths (cache hit, cache miss) and two date
sources (a just-written file, a previously-written file). If that logic spreads
across the CLI runner and the store, the rule is easy to break — and the cache
hit could end up reporting "now" instead of the original fetch date.

## Decision

Orchestrate the cache-aside read-through in one place — `UrlCacheService.get()`.
It checks `CacheStore.exists`, on a miss calls `WebContentFetcher.fetch` then
`CacheStore.write`, and in **both** paths derives the date from
`CacheStore.fetchedDate(...)` so a cache hit returns the original fetch date.

## Consequences

- The "fetch only once" rule lives in exactly one method — easy to read and to test.
- Both paths derive the date the same way; cache hits cannot accidentally report
  the current time.
- The runner stays a thin adapter: invoke the service, print, exit.
- No write-through, refresh-ahead, or TTL — matches the exercise's fetch-once
  scope (see ADR-0003, ADR-0004).
