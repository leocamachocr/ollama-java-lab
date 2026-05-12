package dev.leocamacho.ollamalab.rawapi.application;

import dev.leocamacho.ollamalab.rawapi.domain.OllamaResponse;
import java.util.List;

public interface LlmPort {
    OllamaResponse chat(String systemPrompt, String userMessage);

    List<Double> getEmbedding(String text);
}
