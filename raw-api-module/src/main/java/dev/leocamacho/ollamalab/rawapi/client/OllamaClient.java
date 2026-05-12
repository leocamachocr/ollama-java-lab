package dev.leocamacho.ollamalab.rawapi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.leocamacho.ollamalab.rawapi.model.OllamaEmbeddingRequest;
import dev.leocamacho.ollamalab.rawapi.model.OllamaEmbeddingResponse;
import dev.leocamacho.ollamalab.rawapi.model.OllamaMessage;
import dev.leocamacho.ollamalab.rawapi.model.OllamaRequest;
import dev.leocamacho.ollamalab.rawapi.model.OllamaRequestOptions;
import dev.leocamacho.ollamalab.rawapi.model.OllamaResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP de bajo nivel para la API de Ollama.
 * Encapsula toda la comunicación HTTP y la (de)serialización JSON;
 * el código de los ejemplos no necesita conocer HttpClient, ObjectMapper ni los DTOs internos.
 */
public class OllamaClient {

    // 120 s porque los modelos locales pueden tardar en generar texto largo
    private static final Duration CHAT_TIMEOUT    = Duration.ofSeconds(120);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient   httpClient;
    private final ObjectMapper objectMapper;
    private final String       baseUrl;
    private final String       chatModel;
    private final String       embeddingModel;

    /** Crea un cliente con la configuración por defecto del laboratorio. */
    public static OllamaClient withDefaults() {
        return new OllamaClient("http://localhost:11434", "qwen2.5:7b", "nomic-embed-text");
    }

    public OllamaClient(String baseUrl, String chatModel, String embeddingModel) {
        this.httpClient     = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        this.objectMapper   = new ObjectMapper();
        this.baseUrl        = baseUrl;
        this.chatModel      = chatModel;
        this.embeddingModel = embeddingModel;
    }

    /**
     * Envía un par system/user al endpoint {@code /api/chat} con temperatura 0.7.
     *
     * @param systemPrompt instrucciones de rol y comportamiento para el modelo
     * @param userMessage  texto de entrada del usuario o el prompt de tarea
     */
    public ChatResponse chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, userMessage, 0.7);
    }

    /**
     * Versión con temperatura configurable.
     * Valores bajos (0.0–0.3) producen respuestas más deterministas,
     * recomendado para extraer JSON estructurado.
     */
    public ChatResponse chat(String systemPrompt, String userMessage, double temperature) {
        var requestBody = new OllamaRequest(
            chatModel,
            List.of(
                new OllamaMessage("system", systemPrompt),
                new OllamaMessage("user",   userMessage)
            ),
            false,   // stream: false — esperamos la respuesta completa antes de procesar
            "json",  // format: "json" — instruye al modelo a responder SOLO con JSON válido
            new OllamaRequestOptions(temperature)
        );

        long startTime = System.currentTimeMillis();
        OllamaResponse ollamaResponse = post("/api/chat", requestBody, OllamaResponse.class);
        long latencyMs = System.currentTimeMillis() - startTime;

        return new ChatResponse(
            ollamaResponse.message().content(),
            ollamaResponse.promptEvalCount(),
            ollamaResponse.evalCount(),
            latencyMs
        );
    }

    /**
     * Genera un vector de embeddings para el texto dado usando el endpoint {@code /api/embed}.
     * El vector captura el significado semántico del texto y puede compararse con otros
     * vectores usando similitud coseno para encontrar textos semánticamente relacionados.
     */
    public List<Double> embed(String text) {
        var requestBody = new OllamaEmbeddingRequest(embeddingModel, text);
        OllamaEmbeddingResponse response = post("/api/embed", requestBody, OllamaEmbeddingResponse.class);

        if (response.embeddings() == null || response.embeddings().isEmpty()) {
            // El modelo de chat (e.g. qwen2.5:7b) no produce embeddings;
            // se necesita un modelo especializado como nomic-embed-text
            throw new RuntimeException(
                "El modelo '" + embeddingModel + "' no devolvió embeddings. " +
                "Configura un modelo de embeddings como 'nomic-embed-text'."
            );
        }

        // La API devuelve List<List<Double>> para soportar múltiples inputs en batch;
        // aquí siempre enviamos un solo texto, por eso tomamos el primer (y único) vector
        return response.embeddings().getFirst();
    }

    /** Serializa {@code requestBody} a JSON, hace POST y deserializa la respuesta. */
    private <T> T post(String path, Object requestBody, Class<T> responseType) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .timeout(CHAT_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException(
                    "Ollama respondió con HTTP " + httpResponse.statusCode() +
                    ": " + httpResponse.body()
                );
            }

            return objectMapper.readValue(httpResponse.body(), responseType);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error comunicándose con Ollama en " + baseUrl + path, e);
        }
    }
}
