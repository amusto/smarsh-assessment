package com.example.urlcache.common;

/** Failure reading content or metadata from the local cache. */
public class CacheReadException extends CacheException {
    public CacheReadException(String message, Throwable cause) { super(message, cause); }
}
