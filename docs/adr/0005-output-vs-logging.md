# ADR-0005: Separate program output from troubleshooting logging

Status: Accepted

## Context

The exercise requires printing the result (original date, URL, content) and
separately asks for "relevant prints for troubleshooting."

## Decision

Treat these as two distinct channels:

- **Program output** (the required result) goes to `System.out`.
- **Troubleshooting / diagnostics** (cache hit vs miss, fetch errors) go through
  SLF4J logging at INFO/ERROR.

## Consequences

- The required output stays clean and easy to read or parse.
- Operational detail is in the logs, where it can be leveled and filtered.
- Clear separation of "the result" from "how it was produced."
