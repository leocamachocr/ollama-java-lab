package dev.leocamacho.ollamalab.rawapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaMessage(
    @JsonProperty("role") String role,
    @JsonProperty("content") String content
) {}
