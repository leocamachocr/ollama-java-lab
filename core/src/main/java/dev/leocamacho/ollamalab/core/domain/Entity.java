package dev.leocamacho.ollamalab.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Entity(
    @JsonProperty("value") String value,
    @JsonProperty("type") EntityType type
) {}
