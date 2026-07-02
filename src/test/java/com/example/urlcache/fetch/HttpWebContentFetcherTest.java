package com.example.urlcache.fetch;

import com.example.urlcache.common.InvalidUrlException;
import com.example.urlcache.common.RemoteFetchException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpWebContentFetcherTest {

    private final HttpWebContentFetcher fetcher = new HttpWebContentFetcher();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    /** Starts a local HTTP server returning the given status/body, and returns its URL. */
    private String startServer(int status, String body) throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        return "http://localhost:" + server.getAddress().getPort() + "/";
    }

    @Test
    void malformedUrl_throwsInvalidUrl() {
        // The space is an illegal URI character; URI.create rejects it BEFORE any
        // network call. (Passing the string directly bypasses CLI arg-splitting.)
        assertThatThrownBy(() -> fetcher.fetch("http://exa mpl.com"))
            .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void okResponse_returnsBody() throws Exception {
        String url = startServer(200, "hello world");

        assertThat(fetcher.fetch(url)).isEqualTo("hello world");
    }

    @Test
    void non200Response_throwsRemoteFetch() throws Exception {
        String url = startServer(404, "not found");

        assertThatThrownBy(() -> fetcher.fetch(url))
            .isInstanceOf(RemoteFetchException.class)
            .isNotInstanceOf(InvalidUrlException.class); // it's a remote failure, not a bad URL
    }
}
