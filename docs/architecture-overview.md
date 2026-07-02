# Architecture Overview

url-cache is a small Java CLI (Spring Boot, no web) that fetches a URL's page
source over HTTP, caches it locally as a file, and — on subsequent runs — serves
it from the file instead of the network. Each URL is fetched from the web
**exactly once**. The application takes a URL, reports the *original* fetch
date + URL + content to standard output, and shuts down.

The design keeps the cache logic (`UrlCacheService`) delivery-agnostic behind
two interfaces (`WebContentFetcher`, `CacheStore`) so a second adapter — for
example a web endpoint — can be added without touching the core.

## How to read these docs

The documentation is arranged in three layers, each answering a different
question:

```text
ADR (why)  ->  Pattern (how)  ->  Implementation (the code)
```

- **`docs/adr/`** — Architecture Decision Records. Each captures *why* a
  particular choice was made, what the alternatives were, and the trade-offs.
  Short, dated, immutable once accepted.
- **`docs/patterns/`** — Pattern docs. Each explains *how* the resulting
  pattern works in this codebase and points to the real classes/files that
  realize it. Some are labeled **Proposed — not yet implemented** where they
  describe a decision the code is designed to accept but does not yet do.
- **The code itself** is the source of truth. Every implemented pattern doc
  cites specific files under `src/main/java/...`.

The index — [`assessment-pattern-map.md`](assessment-pattern-map.md) — is the
quickest way to jump between the three layers.

## Repo entry points

- [`README.md`](../README.md) — task, how to run, assumptions, quick architecture.
- [`ROADMAP.md`](../ROADMAP.md) — build sequence and phase status.
- [`docs/adr/`](adr/) — the ADRs.
- [`docs/patterns/`](patterns/) — the pattern docs.
