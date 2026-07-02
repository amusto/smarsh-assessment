package com.example.urlcache.fetch;

import com.example.urlcache.common.InvalidUrlException;
import com.example.urlcache.common.RemoteFetchException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class HttpWebContentFetcher implements WebContentFetcher {

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public String fetch(String url) {
        URI uri;
        // URI.create() throws on malformed URLs, so we catch it here.
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            // Previously uncaught -> crashed the program on a malformed URL.
            throw new InvalidUrlException(url, e);
        }

        try {
            HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RemoteFetchException(
                    "Unexpected status " + response.statusCode() + " for " + url);
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteFetchException("Failed to fetch " + url, e);
        }
    }
}