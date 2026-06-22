# ADR-0004: Use the file's last-modified time as the original fetch date

Status: Accepted

## Context

The program must print the **original** date the content was fetched — including
on cache hits, where the content was first fetched on an earlier run.

## Decision

Derive the fetch date from the cache file's **last-modified timestamp**, read via
`CacheStore.fetchedDate(...)` in *both* the cache-hit and cache-miss paths. No
separate metadata store is kept.

## Consequences

- No extra metadata file or format to maintain — the filesystem already records
  when the file was written.
- The date is genuinely "when the content was first written," and is reported
  consistently on every run.
- Couples the timestamp to filesystem metadata; if portability or richer metadata
  were needed, a sidecar metadata file would be the next step.
