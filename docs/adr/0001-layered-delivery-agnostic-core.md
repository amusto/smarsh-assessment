# ADR-0001: Layered, delivery-agnostic core

Status: Accepted

## Context

The exercise is a CLI program, but requirements change — a likely next step is
exposing cached content over a web interface. If the cache logic were written
inside the CLI entry point, such a change would be a rewrite.

## Decision

Keep the cache logic in `UrlCacheService`, depending only on the
`WebContentFetcher` and `CacheStore` **interfaces**. The `CommandLineRunner`
(`CacheRunner`) is a thin adapter that reads config, invokes the service, and
prints. No delivery concern leaks into the core.

## Consequences

- New entry points (web, batch, scheduled) are **additive adapters**, not rewrites.
- The fetch source and storage mechanism can be swapped behind the interfaces.
- Slightly more files than a single `main` method, but each is small and
  single-purpose — and the seams are exactly where requirements tend to change.
