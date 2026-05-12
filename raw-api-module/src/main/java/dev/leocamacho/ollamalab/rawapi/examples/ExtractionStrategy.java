package dev.leocamacho.ollamalab.rawapi.examples;

/**
 * Estrategias de prompting para extracción de entidades.
 * Cada constante lleva su propio system prompt, evitando cualquier switch externo
 * para seleccionarlo y haciendo que agregar una nueva estrategia sea trivial.
 */
public enum ExtractionStrategy {

    ZERO_SHOT("""
        Extract named entities from the provided text and classify them.

        Entity types:
        - PERSON: people, organizations, companies, institutions
        - CONCEPT: ideas, methodologies, principles, patterns, architectures
        - TECHNOLOGY: programming languages, frameworks, tools, protocols, standards, versions
        - DATE: dates, years, time periods, version release dates

        Return ONLY a valid JSON object. No explanation, no markdown, no extra text.
        Required format: {"entities":[{"value":"entity name","type":"PERSON|CONCEPT|TECHNOLOGY|DATE"}]}
        """),

    FEW_SHOT("""
        Extract named entities from text and classify them. Study these examples:

        Text: "Java 21 was released by Oracle in September 2023."
        Output: {"entities":[{"value":"Java 21","type":"TECHNOLOGY"},{"value":"Oracle","type":"PERSON"},{"value":"September 2023","type":"DATE"}]}

        Text: "The Transformer architecture enables self-attention mechanisms."
        Output: {"entities":[{"value":"Transformer architecture","type":"TECHNOLOGY"},{"value":"self-attention","type":"CONCEPT"}]}

        Text: "Martin Fowler documented microservices patterns at ThoughtWorks in 2014."
        Output: {"entities":[{"value":"Martin Fowler","type":"PERSON"},{"value":"microservices","type":"CONCEPT"},{"value":"ThoughtWorks","type":"PERSON"},{"value":"2014","type":"DATE"}]}

        Now extract entities from the provided text. Return ONLY valid JSON, no other text:
        """),

    CHAIN_OF_THOUGHT("""
        Extract named entities from the provided text following these steps:

        Step 1: Identify all named persons, organizations, and institutions.
        Step 2: Identify all technical terms, frameworks, languages, and tools.
        Step 3: Identify all abstract concepts, methodologies, and principles.
        Step 4: Identify all dates, years, and time periods.
        Step 5: Compile all findings into JSON. Return ONLY the JSON object, nothing else.

        Required format for Step 5 output:
        {"entities":[{"value":"entity name","type":"PERSON|CONCEPT|TECHNOLOGY|DATE"}]}
        """);

    private final String systemPrompt;

    ExtractionStrategy(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String systemPrompt() {
        return systemPrompt;
    }
}
