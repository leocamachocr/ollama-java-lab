package dev.leocamacho.ollamalab.rawapi.config;

public record ExperimentConfig(String ollamaBaseUrl, String model, String embeddingModel) {

    public static ExperimentConfig defaults() {
        return new ExperimentConfig("http://localhost:11434", "qwen2.5:7b", "nomic-embed-text");
    }
}
