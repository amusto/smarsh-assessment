package com.example.urlcache;

import com.example.urlcache.model.CachedContent;
import com.example.urlcache.service.UrlCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CacheRunner implements CommandLineRunner {

    private final UrlCacheService cacheService;
    private final String url;

    public CacheRunner(UrlCacheService cacheService,
                       @Value("${app.url:https://example.com}") String url) {
        this.cacheService = cacheService;
        this.url = url;
    }

    @Override
    public void run(String... args) {
        CachedContent result = cacheService.get(url);

        System.out.println("Original fetch date: " + result.fetchedDate());
        System.out.println("URL: " + result.url());
        System.out.println("Source: " + result.source());
        System.out.println("Content:");
        System.out.println(result.content());
    }
}
