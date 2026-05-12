package dev.leocamacho.ollamalab.rawapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaEmbeddingRequest(
    @JsonProperty("model") String model,
    @JsonProperty("input") String input
) {}

