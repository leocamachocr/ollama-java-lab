package dev.leocamacho.ollamalab.rawapi;

import dev.leocamacho.ollamalab.rawapi.examples.Example02_EntityExtraction;
import dev.leocamacho.ollamalab.rawapi.examples.ExtractionStrategy;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class EntityExtractorIntegrationTest {

    @BeforeAll
    static void requireOllama() {
        Assumptions.assumeTrue(
            OllamaAvailability.isAvailable(),
            "Ollama no disponible en localhost:11434 — test de integración omitido"
        );
    }

    @ParameterizedTest
    @EnumSource(ExtractionStrategy.class)
    void shouldExtractEntitiesWithEachStrategy(ExtractionStrategy strategy) {
        var entities = new Example02_EntityExtraction()
            .extractEntities(SampleTexts.TECHNICAL_TEXT, strategy);

        assertFalse(entities.isEmpty(),
            "Se esperan entidades en el texto técnico (estrategia: " + strategy + ")");

        entities.forEach(entity -> {
            assertNotNull(entity.value());
            assertFalse(entity.value().isBlank());
            assertNotNull(entity.type());
        });
    }
}
