package com.example.urlcache.common;

/** Storage domain failure: reading, writing, or reading metadata from the cache. */
public class CacheException extends UrlCacheException {

    public CacheException(String message) { super(message); }
    public CacheException(String message, Throwable cause) { super(message, cause); }

    @Override public String userMessage() { return "A local cache error occurred."; }
}
