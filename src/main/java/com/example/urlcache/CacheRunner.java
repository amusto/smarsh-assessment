package com.example.urlcache;

import com.example.urlcache.common.ContentFetchException;
import com.example.urlcache.model.CachedContent;
import com.example.urlcache.service.UrlCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class CacheRunner implements CommandLineRunner, ExitCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(CacheRunner.class);

    private final UrlCacheService cacheService;
    private final String url;
    private int exitCode = 0;

    public CacheRunner(UrlCacheService cacheService,
                       @Value("${app.url:https://example.com}") String url) {
        this.cacheService = cacheService;
        this.url = url;
    }

    @Override
    public void run(String... args) {
        try {
            CachedContent result = cacheService.get(url);

            System.out.println("Original fetch date: " + result.fetchedDate());
            System.out.println("URL: " + result.url());
            System.out.println("Source: " + result.source());
            System.out.println("Content:");
            System.out.println(result.content());
        } catch (ContentFetchException e) {
            log.error("Could not retrieve content for {}: {}", url, e.getMessage());
            exitCode = 1;
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
