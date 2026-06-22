package com.example.urlcache.fetch;

public interface WebContentFetcher {
    String fetch(String url);   // returns the page source; throws on failure
}