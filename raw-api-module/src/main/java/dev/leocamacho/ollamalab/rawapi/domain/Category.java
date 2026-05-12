package dev.leocamacho.ollamalab.rawapi.domain;

import java.util.List;

public record Category(
    String name,
    String description,
    List<Double> embedding
) {}

