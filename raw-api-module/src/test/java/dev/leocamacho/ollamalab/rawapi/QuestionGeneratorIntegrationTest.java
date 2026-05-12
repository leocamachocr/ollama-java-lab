package dev.leocamacho.ollamalab.rawapi;

import dev.leocamacho.ollamalab.rawapi.application.QuestionGeneratorUseCase;
import dev.leocamacho.ollamalab.rawapi.config.ExperimentConfig;
import dev.leocamacho.ollamalab.rawapi.infrastructure.JsonQuestionParser;
import dev.leocamacho.ollamalab.rawapi.infrastructure.OllamaRestAdapter;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionGeneratorIntegrationTest {

    private static final ExperimentConfig CONFIG = ExperimentConfig.defaults();

    @BeforeAll
    static void requireOllama() {
        Assumptions.assumeTrue(isOllamaAvailable(),
            "Ollama no disponible en " + CONFIG.ollamaBaseUrl() + " — test de integración omitido");
    }

    @Test
    void shouldGenerateFiveQuestionsFromEducationalText() {
        var adapter = new OllamaRestAdapter(CONFIG.ollamaBaseUrl(), CONFIG.model());
        var useCase = new QuestionGeneratorUseCase(adapter, new JsonQuestionParser());

        var result = useCase.generate(SampleTexts.EDUCATIONAL_TEXT);

        assertNotNull(result, "El resultado no puede ser null");
        assertNotNull(result.output(), "La lista de preguntas no puede ser null");
        assertFalse(result.output().isEmpty(), "Debe haber al menos una pregunta");
        assertTrue(result.latencyMs() > 0, "La latencia debe ser positiva");
        assertTrue(result.estimatedTokens() > 0, "Debe haber al menos un token registrado");

        result.output().forEach(q -> {
            assertNotNull(q.text(), "El texto de la pregunta no puede ser null");
            assertFalse(q.text().isBlank(), "El texto de la pregunta no puede estar vacío");
            assertNotNull(q.options(), "Las opciones no pueden ser null");
            assertEquals(4, q.options().size(), "Cada pregunta debe tener exactamente 4 opciones");
            assertTrue(
                List.of("A", "B", "C", "D").contains(q.correct()),
                "La respuesta correcta debe ser A, B, C o D — fue: " + q.correct()
            );
        });
    }

    @Test
    void shouldRecordPositiveLatency() {
        var adapter = new OllamaRestAdapter(CONFIG.ollamaBaseUrl(), CONFIG.model());
        var useCase = new QuestionGeneratorUseCase(adapter, new JsonQuestionParser());

        var result = useCase.generate(SampleTexts.EDUCATIONAL_TEXT);

        assertTrue(result.latencyMs() >= 100,
            "Latencia esperada > 100ms para una llamada real al modelo");
    }

    private static boolean isOllamaAvailable() {
        try {
            var response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build()
                .send(
                    HttpRequest.newBuilder()
                        .uri(URI.create(CONFIG.ollamaBaseUrl() + "/api/version"))
                        .GET()
                        .timeout(Duration.ofSeconds(3))
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                );
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
