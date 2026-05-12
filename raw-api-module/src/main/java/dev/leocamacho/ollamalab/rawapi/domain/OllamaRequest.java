package dev.leocamacho.ollamalab.rawapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OllamaRequest(
    @JsonProperty("model") String model,
    @JsonProperty("messages") List<OllamaMessage> messages,
    @JsonProperty("stream") boolean stream,
    @JsonProperty("format") String format
) {}
