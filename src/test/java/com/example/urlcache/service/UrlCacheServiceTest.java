package com.example.urlcache.service;

import com.example.urlcache.cache.CacheStore;
import com.example.urlcache.fetch.WebContentFetcher;
import com.example.urlcache.model.CachedContent;
import com.example.urlcache.model.ContentSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlCacheServiceTest {

    @Mock WebContentFetcher fetcher;
    @Mock CacheStore store;
    @InjectMocks UrlCacheService service;

    private static final String URL = "https://example.com";

    @Test
    void cacheMiss_fetchesFromWebAndSaves() {
        Instant now = Instant.now();
        when(store.exists(URL)).thenReturn(false);
        when(fetcher.fetch(URL)).thenReturn("<html>hi</html>");
        when(store.fetchedDate(URL)).thenReturn(now);

        CachedContent result = service.get(URL);

        assertThat(result.source()).isEqualTo(ContentSource.WEB);
        assertThat(result.content()).isEqualTo("<html>hi</html>");
        assertThat(result.fetchedDate()).isEqualTo(now);
        verify(fetcher).fetch(URL);
        verify(store).write(URL, "<html>hi</html>");
    }

    @Test
    void cacheHit_readsFromFileAndDoesNotFetch() {
        Instant earlier = Instant.now();
        when(store.exists(URL)).thenReturn(true);
        when(store.read(URL)).thenReturn("<html>cached</html>");
        when(store.fetchedDate(URL)).thenReturn(earlier);

        CachedContent result = service.get(URL);

        assertThat(result.source()).isEqualTo(ContentSource.CACHE);
        assertThat(result.content()).isEqualTo("<html>cached</html>");
        // The core "fetch only once" guarantee: a cache hit never touches the web.
        verify(fetcher, never()).fetch(anyString());
        verify(store, never()).write(anyString(), anyString());
    }
}
