package com.llmscoring.sdk.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llmscoring.sdk.model.IngestPayload;
import com.llmscoring.sdk.model.ScoringResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ScoringHttpClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final int maxRetries;

    public ScoringHttpClient(String baseUrl, int timeoutSeconds, int maxRetries) {
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }


    public ScoringResponse ingestSync(IngestPayload payload) {
        return requestWithRetry(baseUrl + "/api/v1/events/ingest?sync=true", payload, 200);
    }

    public CompletableFuture<ScoringResponse> ingestAsync(IngestPayload payload) {
        return CompletableFuture.supplyAsync(() ->
                requestWithRetry(baseUrl + "/api/v1/events/ingest", payload, 202));
    }

    private ScoringResponse requestWithRetry(String url, IngestPayload payload,
                                             int expectedStatus) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String json = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == expectedStatus) {
                    if (response.body() != null && !response.body().isBlank()) {
                        return objectMapper.readValue(response.body(), ScoringResponse.class);
                    }
                    return null;
                }

                throw new RuntimeException("Unexpected status: "
                        + response.statusCode() + " — " + response.body());

            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    try { Thread.sleep(500L * attempt); }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new RuntimeException(
                "Failed after " + maxRetries + " attempts", lastException);
    }


    private ScoringResponse ingestWithRetry(IngestPayload payload) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String json = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/api/v1/events/ingest"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 202) {
                    // Async accepted — no body to parse
                    return null;
                }

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(
                            response.body(), ScoringResponse.class);
                }

                throw new RuntimeException("Unexpected status: "
                        + response.statusCode() + " — " + response.body());

            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(500L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new RuntimeException(
                "Failed after " + maxRetries + " attempts", lastException);
    }
}
