package dev.leocamacho.ollamalab.rawapi.application;

import dev.leocamacho.ollamalab.rawapi.domain.Category;
import dev.leocamacho.ollamalab.rawapi.infrastructure.SimilarityCalculator;
import java.util.Comparator;
import java.util.List;

public class TextClassifierUseCase {

    private final LlmPort llmPort;
    private final List<Category> categories;

    public TextClassifierUseCase(LlmPort llmPort, List<Category> categories) {
        this.llmPort = llmPort;
        this.categories = categories;
    }

    public ClassificationResult classify(String text) {
        List<Double> textEmbedding = llmPort.getEmbedding(text);

        return categories.stream()
            .map(category -> new SimilarityScore(
                category.name(),
                category.description(),
                SimilarityCalculator.cosineSimilarity(textEmbedding, category.embedding())
            ))
                //print all results
                .peek(result -> {
                    System.out.println("Category: " + result.categoryName());
                    System.out.println("Description: " + result.description());
                    System.out.println("Similarity: " + result.score());
                })
            .max(Comparator.comparingDouble(SimilarityScore::score))
            .map(score -> new ClassificationResult(score.categoryName(), score.score(), score.description()))
            .orElseThrow(() -> new RuntimeException("No categories available"));
    }

    private record SimilarityScore(String categoryName, String description, double score) {}
}


