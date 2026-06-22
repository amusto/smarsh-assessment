# ADR-0008: Handle fetch failures at the adapter boundary

Status: Accepted

## Context

A failed fetch (unreachable host, non-200 status) is a normal outcome of running
this program, not a bug. Letting it propagate as an uncaught exception would
print a stack-trace dump that obscures what actually happened.

## Decision

Catch `ContentFetchException` at the adapter boundary (`CacheRunner.run`), log a
single clear error line, and let the program exit normally. The core service
keeps signalling failure with an unchecked exception; only the adapter knows
what "handled" means for its delivery context.

## Consequences

- Failures read as one tidy log line, not a stack trace.
- Nothing is written to the cache on failure (the write only happens after a
  successful fetch).
- A different adapter (e.g. a future web endpoint) gets to choose its own
  policy — HTTP 502, retry, etc. — without the core caring.
- Exit-code policy and the rationale for "exit 0" are documented in the README
  ("Assumptions", #9); not duplicated here.
