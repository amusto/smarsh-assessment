package com.example.urlcache.fetch;

import com.example.urlcache.common.ContentFetchException;
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
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ContentFetchException(
                    "Unexpected status " + response.statusCode() + " for " + url);
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();   // restore interrupt flag
            throw new ContentFetchException("Failed to fetch " + url, e);
        }
    }
}