package dev.leocamacho.ollamalab.rawapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaEmbeddingResponse(
    @JsonProperty("embeddings") List<List<Double>> embeddings,
    @JsonProperty("model")      String model
) {}
