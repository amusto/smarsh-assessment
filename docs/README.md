# url-cache — Architecture Docs

A Java CLI that fetches a URL's content from the web **once**, caches it locally
as a file, and serves it from that file on subsequent runs — printing the
original fetch date, the URL, and the content.

## How to read these docs

The docs are arranged in three layers:

```text
ADR (why)  ->  Pattern (how)  ->  Implementation (the code)
```

- The [**Architecture overview**](architecture-overview.md) is the one-page
  system summary.
- The [**Assessment pattern map**](assessment-pattern-map.md) is the index —
  every ADR ↔ Pattern ↔ file mapping in a single table.
- The **ADRs** (sidebar) capture the *why* behind each decision.
- The **Patterns** (sidebar) explain *how* each pattern works in this
  codebase and cite the real classes. One pattern — the web adapter —
  is labeled **Proposed — not yet implemented**.

For build, run, and test instructions, see `README.md` and `ROADMAP.md` in the
repository root.
