package com.example.urlcache.model;

import java.time.Instant;

public record CachedContent(
    String url,
    String content,
    Instant fetchedDate,   // the "original date" — from the file's last-modified
    ContentSource source   // WEB or CACHE
) {}