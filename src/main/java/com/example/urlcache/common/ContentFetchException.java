package com.example.urlcache.common;

public class ContentFetchException extends RuntimeException {
    public ContentFetchException(String message) { super(message); }
    public ContentFetchException(String message, Throwable cause) { super(message, cause); }
}