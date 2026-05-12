package dev.leocamacho.ollamalab.rawapi;

import dev.leocamacho.ollamalab.rawapi.application.EntityExtractorUseCase;
import dev.leocamacho.ollamalab.rawapi.application.ExtractionStrategy;
import dev.leocamacho.ollamalab.rawapi.config.ExperimentConfig;
import dev.leocamacho.ollamalab.rawapi.infrastructure.JsonEntityParser;
import dev.leocamacho.ollamalab.rawapi.infrastructure.OllamaRestAdapter;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class EntityExtractorIntegrationTest {

    private static final ExperimentConfig CONFIG = ExperimentConfig.defaults();

    @BeforeAll
    static void requireOllama() {
        Assumptions.assumeTrue(isOllamaAvailable(),
            "Ollama no disponible en " + CONFIG.ollamaBaseUrl() + " — test de integración omitido");
    }

    @ParameterizedTest
    @EnumSource(ExtractionStrategy.class)
    void shouldExtractEntitiesWithEachStrategy(ExtractionStrategy strategy) {
        var adapter = new OllamaRestAdapter(CONFIG.ollamaBaseUrl(), CONFIG.model());
        var useCase = new EntityExtractorUseCase(adapter, new JsonEntityParser());

        var result = useCase.extract(SampleTexts.TECHNICAL_TEXT, strategy);

        assertNotNull(result, "El resultado no puede ser null");
        assertNotNull(result.output(), "La lista de entidades no puede ser null");
        assertFalse(result.output().isEmpty(),
            "Se esperan entidades en el texto técnico (estrategia: " + strategy + ")");
        assertTrue(result.latencyMs() > 0, "La latencia debe ser positiva");

        result.output().forEach(entity -> {
            assertNotNull(entity.value(), "El valor de la entidad no puede ser null");
            assertFalse(entity.value().isBlank(), "El valor de la entidad no puede estar vacío");
            assertNotNull(entity.type(), "El tipo de la entidad no puede ser null");
        });
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
