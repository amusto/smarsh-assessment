# ADR-0002: Use the JDK HttpClient (no third-party HTTP library)

Status: Accepted

## Context

The program fetches page source over HTTP. Options include third-party libraries
(OkHttp, Apache HttpClient) or the JDK's built-in `java.net.http.HttpClient`
(Java 11+).

## Decision

Use `java.net.http.HttpClient` from the JDK.

## Consequences

- Zero extra dependencies — smaller, simpler build, less to justify.
- Modern and more than sufficient for a single GET of a page's source.
- If advanced needs arose (retries, connection-pool tuning, interceptors), a
  dedicated library could be revisited; not warranted here.
