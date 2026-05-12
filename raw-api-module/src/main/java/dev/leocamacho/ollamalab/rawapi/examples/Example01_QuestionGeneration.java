package dev.leocamacho.ollamalab.rawapi.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.leocamacho.ollamalab.core.domain.Question;
import dev.leocamacho.ollamalab.rawapi.SampleTexts;
import dev.leocamacho.ollamalab.rawapi.client.OllamaClient;
import java.util.List;

/**
 * Ejemplo 01 — Generación de preguntas con output JSON estructurado.
 *
 * Demuestra cómo usar el parámetro {@code format: "json"} del endpoint /api/chat
 * de Ollama para obtener respuestas estrictamente en JSON, y cómo deserializarlas
 * en objetos de dominio tipados con Jackson.
 */
public class Example01_QuestionGeneration {

    private static final String SYSTEM_PROMPT = """
        You are an educational assessment expert. Generate exactly 5 multiple-choice quiz questions
        based on the provided text.

        Rules:
        - Each question must test a specific fact from the text
        - Each question has exactly 4 options labeled A, B, C, D
        - Exactly one option is correct
        - Options must be plausible and similar in length
        - The correct field must be a single letter: A, B, C, or D

        Return ONLY a valid JSON object. No explanation, no markdown, no extra text.
        Required format:
        {"questions":[{"text":"Question?","options":["A) ...","B) ...","C) ...","D) ..."],"correct":"A"}]}
        """;

    /** Ejecuta el ejemplo completo con salida descriptiva paso a paso. */
    public void run() {
        System.out.println("═══ Ejemplo 01: Generación de preguntas con JSON estructurado ═══\n");
        System.out.println("→ Texto de entrada: párrafo sobre la JVM (Java Virtual Machine)");
        System.out.println("→ Enviando prompt al modelo qwen2.5:7b...\n");

        var client = OllamaClient.withDefaults();
        var chatResponse = client.chat(SYSTEM_PROMPT, SampleTexts.EDUCATIONAL_TEXT);

        System.out.printf("← Respuesta recibida en %dms | ~%d tokens totales%n%n",
            chatResponse.latencyMs(), chatResponse.totalTokens());

        var questions = parseQuestions(chatResponse.content());
        System.out.printf("← %d preguntas generadas:%n%n", questions.size());
        questions.forEach(this::printQuestion);
    }

    /** Genera preguntas para el texto dado. Expuesto para los tests de integración. */
    public List<Question> generateQuestions(String inputText) {
        var chatResponse = OllamaClient.withDefaults().chat(SYSTEM_PROMPT, inputText);
        return parseQuestions(chatResponse.content());
    }

    private List<Question> parseQuestions(String jsonContent) {
        try {
            var mapper = new ObjectMapper();
            var questionsNode = mapper.readTree(jsonContent).get("questions");
            if (questionsNode == null || !questionsNode.isArray()) {
                throw new RuntimeException("Response missing 'questions' array. Raw: " + jsonContent);
            }
            return mapper.convertValue(
                questionsNode,
                mapper.getTypeFactory().constructCollectionType(List.class, Question.class)
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse question JSON", e);
        }
    }

    private void printQuestion(Question question) {
        System.out.println("  P: " + question.text());
        question.options().forEach(option -> System.out.println("     " + option));
        System.out.println("     Correcta: " + question.correct() + "\n");
    }
}
