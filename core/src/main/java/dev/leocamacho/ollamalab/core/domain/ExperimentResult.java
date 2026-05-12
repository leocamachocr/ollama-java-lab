package dev.leocamacho.ollamalab.core.domain;

public record ExperimentResult<T>(
    T output,
    long latencyMs,
    String rawResponse,
    int estimatedTokens
) {}
