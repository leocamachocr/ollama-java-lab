package dev.leocamacho.ollamalab.rawapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Utilidad compartida por los tests de integración para verificar si Ollama está disponible. */
final class OllamaAvailability {

    private static final String OLLAMA_URL = "http://localhost:11434";

    private OllamaAvailability() {}

    static boolean isAvailable() {
        try {
            var response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build()
                .send(
                    HttpRequest.newBuilder()
                        .uri(URI.create(OLLAMA_URL + "/api/version"))
                        .GET()
                        .timeout(Duration.ofSeconds(3))
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                );
            return response.statusCode() == 200;
        } catch (Exception ignored) {
            return false;
        }
    }
}
