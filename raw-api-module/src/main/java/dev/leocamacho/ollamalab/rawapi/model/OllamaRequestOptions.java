package dev.leocamacho.ollamalab.rawapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaRequestOptions(
    @JsonProperty("temperature") double temperature
) {}
