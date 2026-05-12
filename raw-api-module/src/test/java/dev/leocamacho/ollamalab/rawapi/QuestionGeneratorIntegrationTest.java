package dev.leocamacho.ollamalab.rawapi;

import dev.leocamacho.ollamalab.rawapi.examples.Example01_QuestionGeneration;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionGeneratorIntegrationTest {

    @BeforeAll
    static void requireOllama() {
        Assumptions.assumeTrue(
            OllamaAvailability.isAvailable(),
            "Ollama no disponible en localhost:11434 — test de integración omitido"
        );
    }

    @Test
    void shouldGenerateQuestionsFromEducationalText() {
        var questions = new Example01_QuestionGeneration()
            .generateQuestions(SampleTexts.EDUCATIONAL_TEXT);

        assertFalse(questions.isEmpty(), "Debe generarse al menos una pregunta");

        questions.forEach(question -> {
            assertNotNull(question.text());
            assertFalse(question.text().isBlank());
            assertEquals(4, question.options().size(),
                "Cada pregunta debe tener exactamente 4 opciones");
            assertTrue(
                List.of("A", "B", "C", "D").contains(question.correct()),
                "La respuesta correcta debe ser A, B, C o D — fue: " + question.correct()
            );
        });
    }
}
