package dev.leocamacho.ollamalab.rawapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaMessage(
    @JsonProperty("role")    String role,
    @JsonProperty("content") String content
) {}
