package com.example.urlcache;

import com.example.urlcache.common.ContentFetchException;
import com.example.urlcache.service.UrlCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheRunnerTest {

    @Mock
    UrlCacheService service;

    private static final String URL = "https://does-not-exist.example";

    @Test
    void run_whenFetchFails_handlesGracefullyWithoutCrashing() {
        CacheRunner runner = new CacheRunner(service, URL);
        when(service.get(URL)).thenThrow(new ContentFetchException("Failed to fetch " + URL));

        // A fetch failure is caught and logged, not propagated — the program
        // does not crash with a stack-trace dump.
        assertThatCode(runner::run).doesNotThrowAnyException();
    }

    @Test
    void run_whenNoUrlProvided_logsUsageAndDoesNotCallService() {
        CacheRunner runner = new CacheRunner(service, "");

        runner.run();

        // A missing URL is a usage error: the program must not attempt a fetch.
        verifyNoInteractions(service);
    }
}
