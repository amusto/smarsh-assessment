package com.example.urlcache.common;

/**
 * Base for all application-level failures in url-cache.
 *
 * Delivery-agnostic on purpose: it carries SEMANTICS the boundary can act on
 * (a safe user-facing message, whether the failure is retryable) but knows
 * nothing about transport concerns such as HTTP status codes or process exit
 * codes. Each inbound boundary maps the type to its own response.
 */
public abstract class UrlCacheException extends RuntimeException {

    protected UrlCacheException(String message) { super(message); }
    protected UrlCacheException(String message, Throwable cause) { super(message, cause); }

    /** Safe, client-facing text. Never leaks internals. */
    public String userMessage() { return "The request could not be completed."; }

    /** Semantic hint a boundary can act on (e.g. Kafka retry vs dead-letter). */
    public boolean isRetryable() { return false; }
}
