package com.example.urlcache.common;

/** Failure writing content to the local cache (incl. permission denied). */
public class CacheWriteException extends CacheException {
    public CacheWriteException(String message, Throwable cause) { super(message, cause); }
}
