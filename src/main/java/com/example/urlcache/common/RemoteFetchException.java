package com.example.urlcache.common;

/** Fetch / remote domain failure: network error or non-200 response. */
public class RemoteFetchException extends UrlCacheException {

    public RemoteFetchException(String message) { super(message); }
    public RemoteFetchException(String message, Throwable cause) { super(message, cause); }

    @Override public String userMessage() { return "The remote content could not be retrieved."; }
    @Override public boolean isRetryable() { return true; }   // transient network issues
}
