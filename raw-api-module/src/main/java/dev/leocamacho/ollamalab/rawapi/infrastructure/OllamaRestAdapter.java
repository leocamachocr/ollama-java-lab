package dev.leocamacho.ollamalab.rawapi.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.leocamacho.ollamalab.rawapi.application.LlmPort;
import dev.leocamacho.ollamalab.rawapi.domain.OllamaEmbeddingRequest;
import dev.leocamacho.ollamalab.rawapi.domain.OllamaEmbeddingResponse;
import dev.leocamacho.ollamalab.rawapi.domain.OllamaMessage;
import dev.leocamacho.ollamalab.rawapi.domain.OllamaRequest;
import dev.leocamacho.ollamalab.rawapi.domain.OllamaResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class OllamaRestAdapter implements LlmPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String model;
    private final String embeddingModel;

    public OllamaRestAdapter(String baseUrl, String model) {
        this(baseUrl, model, model);
    }

    public OllamaRestAdapter(String baseUrl, String model, String embeddingModel) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.model = model;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public OllamaResponse chat(String systemPrompt, String userMessage) {
        try {
            var request = new OllamaRequest(
                model,
                List.of(
                    new OllamaMessage("system", systemPrompt),
                    new OllamaMessage("user", userMessage)
                ),
                false,
                "json"
            );

            String requestBody = objectMapper.writeValueAsString(request);

            var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException(
                    "Ollama returned HTTP " + httpResponse.statusCode() + ": " + httpResponse.body()
                );
            }

            return objectMapper.readValue(httpResponse.body(), OllamaResponse.class);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with Ollama at " + baseUrl, e);
        }
    }

    @Override
    public List<Double> getEmbedding(String text) {
        try {
            var request = new OllamaEmbeddingRequest(embeddingModel, text);
            String requestBody = objectMapper.writeValueAsString(request);

            var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/embed"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException(
                    "Ollama embedding failed with HTTP " + httpResponse.statusCode() + ": " + httpResponse.body()
                );
            }

            OllamaEmbeddingResponse response = objectMapper.readValue(
                httpResponse.body(),
                OllamaEmbeddingResponse.class
            );
            if (response.embeddings() == null || response.embeddings().isEmpty()) {
                throw new RuntimeException(
                    "Embedding response is empty. Model '" + embeddingModel + "' may not support embeddings. " +
                    "Use an embedding model like 'nomic-embed-text'. " +
                    "Raw response: " + httpResponse.body()
                );
            }
            return response.embeddings().getFirst();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get embedding from Ollama", e);
        }
    }
}
