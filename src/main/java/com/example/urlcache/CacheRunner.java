package com.example.urlcache;

import com.example.urlcache.common.ContentFetchException;
import com.example.urlcache.model.CachedContent;
import com.example.urlcache.service.UrlCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CacheRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CacheRunner.class);
    private static final String RUN_ID = "runId";

    private final UrlCacheService cacheService;
    private final String url;

    public CacheRunner(UrlCacheService cacheService,
                       @Value("${app.url:https://example.com}") String url) {
        this.cacheService = cacheService;
        this.url = url;
    }

    @Override
    public void run(String... args) {
        // One correlation ID per run, so every log line for this execution can be
        // traced together (the CLI analog of a per-request correlation ID).
        MDC.put(RUN_ID, UUID.randomUUID().toString());
        try {
            CachedContent result = cacheService.get(url);

            System.out.println("Original fetch date: " + result.fetchedDate());
            System.out.println("URL: " + result.url());
            System.out.println("Source: " + result.source());
            System.out.println("Content:");
            System.out.println(result.content());
        } catch (ContentFetchException e) {
            // A fetch failure is a handled outcome, not a crash: log a clear
            // message and let the program exit normally.
            log.error("Could not retrieve content for {}: {}", url, e.getMessage());
        } finally {
            MDC.remove(RUN_ID);
        }
    }
}
