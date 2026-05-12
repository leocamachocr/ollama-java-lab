package dev.leocamacho.ollamalab.rawapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaResponse(
    @JsonProperty("model")             String model,
    @JsonProperty("message")           OllamaMessage message,
    @JsonProperty("done")              boolean done,
    @JsonProperty("total_duration")    long totalDuration,
    @JsonProperty("prompt_eval_count") int promptEvalCount,
    @JsonProperty("eval_count")        int evalCount
) {}
