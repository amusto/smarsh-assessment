# url-cache — Architecture Decisions

A Java CLI that fetches a URL's content from the web **once**, caches it locally
as a file, and serves it from that file on subsequent runs — printing the
original fetch date, the URL, and the content.

Use the sidebar to browse the **Architecture Decision Records (ADRs)** that
capture the key design choices: the delivery-agnostic core, the JDK HttpClient,
the SHA-256 cache filename, last-modified as the fetch date, the output-vs-logging
split, and the per-run correlation ID.

For build, run, and test instructions, see `README.md` and `ROADMAP.md` in the
repository root.
