package com.example.urlcache;

import com.example.urlcache.common.ContentFetchException;
import com.example.urlcache.model.CachedContent;
import com.example.urlcache.model.ContentSource;
import com.example.urlcache.service.UrlCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheRunnerTest {

    @Mock
    UrlCacheService service;

    private static final String URL = "https://does-not-exist.example";

    @Test
    void run_whenFetchFails_handlesGracefullyAndSetsNonZeroExit() {
        CacheRunner runner = new CacheRunner(service, URL);
        when(service.get(URL)).thenThrow(new ContentFetchException("Failed to fetch " + URL));

        // The runner must NOT propagate the exception (no crash/stack-trace dump)...
        assertThatCode(runner::run).doesNotThrowAnyException();

        // ...and it signals failure via a non-zero exit code.
        assertThat(runner.getExitCode()).isEqualTo(1);
    }

    @Test
    void run_whenSuccessful_exitCodeIsZero() {
        CacheRunner runner = new CacheRunner(service, URL);
        when(service.get(URL))
            .thenReturn(new CachedContent(URL, "content", Instant.now(), ContentSource.WEB));

        runner.run();

        assertThat(runner.getExitCode()).isZero();
    }
}
