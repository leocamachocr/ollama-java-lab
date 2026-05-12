package dev.leocamacho.ollamalab.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Question(
    @JsonProperty("text") String text,
    @JsonProperty("options") List<String> options,
    @JsonProperty("correct") String correct
) {}
