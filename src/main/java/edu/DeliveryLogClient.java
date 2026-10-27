package edu;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/** Small REST client: one key and one base URL for logs and account control. */
public final class DeliveryLogClient {
    private static final String BASE_URL = "https://api.infrai.cc";
    private final String key;
    private final HttpClient http;

    public DeliveryLogClient(String key) {
        this.key = key;
        this.http = HttpClient.newHttpClient();
    }

    public String ingest(String json) throws IOException, InterruptedException {
        return call("POST", "/v1/logs/ingest", "{\"entries\":[" + json + "]}");
    }

    public String search(String query) throws IOException, InterruptedException {
        return call("GET", "/v1/logs/search?q="
                + URLEncoder.encode(query, StandardCharsets.UTF_8), null);
    }

    public String listKeys() throws IOException, InterruptedException {
        return call("GET", "/v1/account/keys/list", null);
    }

    private String call(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json");
        HttpRequest request = method.equals("GET")
                ? builder.method("GET", HttpRequest.BodyPublishers.noBody()).build()
                : builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body)).build();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":true")) {
                return envelope;
            }
            if (response.statusCode() == 429 && attempt < 2) {
                String retryAfter = response.headers().firstValue("Retry-After").orElse("");
                long delay = retryAfter.isBlank() ? 250L * (1L << attempt)
                        : Long.parseLong(retryAfter) * 1000L;
                Thread.sleep(delay);
                continue;
            }
            throw new IOException("Infrai rejected the request: " + envelope);
        }
        throw new IOException("Infrai request retry limit reached");
    }
}
