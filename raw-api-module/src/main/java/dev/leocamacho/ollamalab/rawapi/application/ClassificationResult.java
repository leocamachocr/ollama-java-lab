package dev.leocamacho.ollamalab.rawapi.application;

public record ClassificationResult(
    String categoryName,
    double similarity,
    String description
) {}

