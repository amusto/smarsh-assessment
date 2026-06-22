# ADR-0006: Per-run correlation ID (runId) for traceability

Status: Accepted

## Context

In a request/response service, a per-request correlation ID lets you trace one
request across log lines. This program is a one-shot CLI: the unit of work is a
**run**, not a request — so a per-request ID does not apply directly.

## Decision

Generate a UUID `runId` at the start of each run, store it in **MDC**, surface it
in the log pattern (`[runId=...]`), and clear it in a `finally` block. The
correlation unit is the run, not the request.

## Consequences

- Every log line for one execution shares an ID, so a single run's trail can be
  isolated (e.g. `grep "runId=<id>"`) — valuable once runs are scheduled or logs
  aggregated.
- Negligible cost; complements the `source` (WEB|CACHE) provenance on the model.
- If a web adapter is added later (ADR-0001), a per-*request* correlation filter
  would complement this per-*run* ID.
