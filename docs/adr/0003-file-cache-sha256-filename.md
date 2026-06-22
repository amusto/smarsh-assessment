# ADR-0003: File-based cache keyed by SHA-256 of the URL

Status: Accepted

## Context

Each URL's content is cached locally as a file. A raw URL is not a safe or
bounded filename (illegal characters, length limits).

## Decision

Store one file per URL in a cache directory. The filename is the hex-encoded
**SHA-256 of the URL** with a `.txt` extension. The hashing is an implementation
detail inside `FileCacheStore`; the `CacheStore` interface speaks in URLs.

## Consequences

- Deterministic, collision-resistant, filesystem-legal names regardless of URL
  length or characters.
- Simple, direct lookups.
- The original URL is not recoverable from the filename — acceptable, since the
  program is always keyed by the URL it is given.
- No eviction or TTL (see ADR-0004 / fetch-once scope).
