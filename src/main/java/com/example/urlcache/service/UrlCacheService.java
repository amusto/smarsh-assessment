package com.example.urlcache.service;

import com.example.urlcache.cache.CacheStore;
import com.example.urlcache.fetch.WebContentFetcher;
import com.example.urlcache.model.CachedContent;
import com.example.urlcache.model.ContentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UrlCacheService {

    private static final Logger log = LoggerFactory.getLogger(UrlCacheService.class);

    private final WebContentFetcher fetcher;
    private final CacheStore store;

    public UrlCacheService(WebContentFetcher fetcher, CacheStore store) {
        this.fetcher = fetcher;
        this.store = store;
    }

    public CachedContent get(String url) {
        ContentSource source;
        String content;

        if (store.exists(url)) {
            log.info("Cache hit for {} - reading from local file", url);
            content = store.read(url);
            source = ContentSource.CACHE;
        } else {
            log.info("Cache miss for {} - fetching from web", url);
            content = fetcher.fetch(url);
            store.write(url, content);
            log.info("Saved content for {} to cache", url);
            source = ContentSource.WEB;
        }

        // Derive the original fetch date from the stored file in BOTH paths,
        // so a cache hit reports the first-fetched time, not "now".
        Instant fetchedDate = store.fetchedDate(url);

        return new CachedContent(url, content, fetchedDate, source);
    }
}
