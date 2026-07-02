# Assessment Pattern Map

The chain the docs enforce:

```text
ADR (why)  ->  Pattern (how)  ->  Implementation (the code)
```

The table below is the single-page index for jumping between layers. Every
row with `Status: Implemented` cites files that exist in the repo today; rows
with `Status: Proposed` describe forward-looking patterns clearly labeled in
the pattern doc itself.

| # | Pattern | Related ADR(s) | Status | Key files |
|---|---|---|---|---|
| 1 | [Ports & Adapters (delivery-agnostic core)](patterns/ports-and-adapters.md) | [ADR-0001](adr/0001-layered-delivery-agnostic-core.md) | Implemented | `CacheRunner.java`, `UrlCacheService.java`, `WebContentFetcher.java`, `CacheStore.java`, `HttpWebContentFetcher.java`, `FileCacheStore.java` |
| 2 | [Cache-aside read-through](patterns/cache-aside.md) | [ADR-0007](adr/0007-cache-aside-read-through.md), [ADR-0004](adr/0004-last-modified-as-fetch-date.md) | Implemented | `UrlCacheService.java`, `FileCacheStore.java` |
| 3 | [Delivery-agnostic exception hierarchy](patterns/delivery-agnostic-exceptions.md) | [ADR-0008](adr/0008-graceful-failure-at-boundary.md) | Implemented | `common/UrlCacheException.java`, `common/CacheException.java`, `common/CacheReadException.java`, `common/CacheWriteException.java`, `common/RemoteFetchException.java`, `common/InvalidUrlException.java`, `CacheRunner.java` |
| 4 | [Correlation-ID traceability (per-run)](patterns/correlation-id-traceability.md) | [ADR-0006](adr/0006-per-run-correlation-id.md) | Implemented | `CacheRunner.java`, `application.properties` |
| 5 | [Program output vs. diagnostics](patterns/output-vs-diagnostics.md) | [ADR-0005](adr/0005-output-vs-logging.md) | Implemented | `CacheRunner.java`, `UrlCacheService.java` |
| 6 | [Web adapter (second driving adapter)](patterns/web-adapter.md) | Extends [ADR-0001](adr/0001-layered-delivery-agnostic-core.md), [ADR-0006](adr/0006-per-run-correlation-id.md), [ADR-0008](adr/0008-graceful-failure-at-boundary.md) | **Proposed — not yet implemented** | (none yet; would live behind a `web` profile) |

## Reading order

If you have five minutes, read them in this order:

1. [Ports & Adapters](patterns/ports-and-adapters.md) — the shape of the system.
2. [Cache-aside read-through](patterns/cache-aside.md) — the one rule the whole
   program exists to enforce.
3. [Delivery-agnostic exceptions](patterns/delivery-agnostic-exceptions.md) —
   how failure signals travel from core to boundary.

The rest are supporting patterns.

## Explicit gaps (forward-looking)

Only one pattern is currently forward-looking:

- **Web adapter** — Phase 11 stretch in the [ROADMAP](../ROADMAP.md). The seams
  in the core (`UrlCacheService`, interfaces, exception hierarchy) already
  admit this adapter; the pattern doc describes what would be added and where.
