package dev.leocamacho.ollamalab.rawapi.application;

import dev.leocamacho.ollamalab.core.domain.Entity;
import dev.leocamacho.ollamalab.core.domain.ExperimentResult;
import dev.leocamacho.ollamalab.rawapi.infrastructure.JsonEntityParser;
import java.util.List;

public class EntityExtractorUseCase {

    private static final String ZERO_SHOT_PROMPT = """
            Extract named entities from the provided text and classify them.

            Entity types:
            - PERSON: people, organizations, companies, institutions
            - CONCEPT: ideas, methodologies, principles, patterns, architectures
            - TECHNOLOGY: programming languages, frameworks, tools, protocols, standards, versions
            - DATE: dates, years, time periods, version release dates

            Return ONLY a valid JSON object. No explanation, no markdown, no extra text.

            Required format:
            {"entities":[{"value":"entity name","type":"PERSON|CONCEPT|TECHNOLOGY|DATE"}]}
            """;

    private static final String FEW_SHOT_PROMPT = """
            Extract named entities from text and classify them. Study these examples:

            Text: "Java 21 was released by Oracle in September 2023."
            Output: {"entities":[{"value":"Java 21","type":"TECHNOLOGY"},{"value":"Oracle","type":"PERSON"},{"value":"September 2023","type":"DATE"}]}

            Text: "The Transformer architecture enables self-attention mechanisms."
            Output: {"entities":[{"value":"Transformer architecture","type":"TECHNOLOGY"},{"value":"self-attention","type":"CONCEPT"}]}

            Text: "Martin Fowler documented microservices patterns at ThoughtWorks in 2014."
            Output: {"entities":[{"value":"Martin Fowler","type":"PERSON"},{"value":"microservices","type":"CONCEPT"},{"value":"ThoughtWorks","type":"PERSON"},{"value":"2014","type":"DATE"}]}

            Now extract entities from the provided text. Return ONLY valid JSON, no other text:
            """;

    private static final String CHAIN_OF_THOUGHT_PROMPT = """
            Extract named entities from the provided text following these steps:

            Step 1: Identify all named persons, organizations, and institutions.
            Step 2: Identify all technical terms, frameworks, languages, and tools.
            Step 3: Identify all abstract concepts, methodologies, and principles.
            Step 4: Identify all dates, years, and time periods.
            Step 5: Compile all findings into JSON. Return ONLY the JSON object, nothing else.

            Required format for Step 5 output:
            {"entities":[{"value":"entity name","type":"PERSON|CONCEPT|TECHNOLOGY|DATE"}]}
            """;

    private final LlmPort llmPort;
    private final JsonEntityParser parser;

    public EntityExtractorUseCase(LlmPort llmPort, JsonEntityParser parser) {
        this.llmPort = llmPort;
        this.parser = parser;
    }

    public ExperimentResult<List<Entity>> extract(String inputText) {
        return extract(inputText, ExtractionStrategy.ZERO_SHOT);
    }

    public ExperimentResult<List<Entity>> extract(String inputText, ExtractionStrategy strategy) {
        String systemPrompt = switch (strategy) {
            case ZERO_SHOT -> ZERO_SHOT_PROMPT;
            case FEW_SHOT -> FEW_SHOT_PROMPT;
            case CHAIN_OF_THOUGHT -> CHAIN_OF_THOUGHT_PROMPT;
        };

        long start = System.currentTimeMillis();
        var response = llmPort.chat(systemPrompt, inputText);
        long latencyMs = System.currentTimeMillis() - start;

        var entities = parser.parse(response.message().content());
        int estimatedTokens = response.promptEvalCount() + response.evalCount();

        return new ExperimentResult<>(entities, latencyMs, response.message().content(), estimatedTokens);
    }
}
