package com.example.urlcache.common;

/**
 * Base for all application-level failures in url-cache.
 *
 * Delivery-agnostic on purpose: it carries SEMANTICS the boundary can act on
 * (a safe user-facing message, whether the failure is retryable) but knows
 * nothing about transport concerns such as HTTP status codes or process exit
 * codes. Each inbound boundary maps the type to its own response.
 */
public class InvalidUrlException extends RemoteFetchException {

    public InvalidUrlException(String url, Throwable cause) {
        super("Invalid URL: " + url, cause);
    }

    @Override public String userMessage() { return "The URL provided is not valid."; }
    @Override public boolean isRetryable() { return false; }

}
