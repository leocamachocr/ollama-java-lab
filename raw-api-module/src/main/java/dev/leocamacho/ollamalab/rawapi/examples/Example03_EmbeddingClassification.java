package dev.leocamacho.ollamalab.rawapi.examples;

import dev.leocamacho.ollamalab.rawapi.client.OllamaClient;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ejemplo 03 — Clasificación de texto sin etiquetas usando embeddings y similitud coseno.
 *
 * Demuestra el endpoint /api/embed de Ollama: genera vectores semánticos para un
 * conjunto de categorías y para cada texto de prueba, luego clasifica el texto
 * buscando la categoría con mayor similitud coseno. No requiere entrenamiento ni fine-tuning.
 */
public class Example03_EmbeddingClassification {

    private record Category(String name, String description) {}
    private record ClassificationResult(String categoryName, double similarity) {}

    private static final List<Category> CATEGORIES = List.of(
        new Category("tecnología",  "texto sobre programación, software, lenguajes, frameworks o herramientas de desarrollo"),
        new Category("deportes",    "texto sobre partido, equipo deportivo, atleta, competición o torneo"),
        new Category("viajes",      "texto sobre turismo, destinos, hoteles, vuelos, playas o montañas"),
        new Category("gastronomía", "texto sobre cocina, recetas, restaurantes, ingredientes o platos típicos")
    );

    private static final List<String> TEST_TEXTS = List.of(
        "Java 21 es un lenguaje de programación muy poderoso",
        "El FC Barcelona jugará un partido de fútbol importante",
        "Reservamos un viaje a las playas de Bali",
        "Hicimos una deliciosa paella con mariscos frescos",
        "Python y React son tecnologías populares",
        "Michael Phelps fue un nadador olímpico excepcional"
    );

    /** Ejecuta el flujo completo: embeber categorías y clasificar cada texto de prueba. */
    public void run() {
        var client = OllamaClient.withDefaults();
        System.out.println("═══ Ejemplo 03: Clasificación por similitud de embeddings ═══\n");

        System.out.println("→ Paso 1: generando embeddings para cada categoría...");
        var categoryEmbeddings = embedCategories(client);
        System.out.println("← " + categoryEmbeddings.size() + " vectores generados\n");

        System.out.println("→ Paso 2: clasificando textos de prueba...\n");
        for (String text : TEST_TEXTS) {
            var result = classifyText(client, text, categoryEmbeddings);
            System.out.printf("  \"%s\"%n  → %s (similitud: %.3f)%n%n",
                text, result.categoryName(), result.similarity());
        }
    }

    private Map<Category, List<Double>> embedCategories(OllamaClient client) {
        // LinkedHashMap preserva el orden de inserción para que los prints sean consistentes
        Map<Category, List<Double>> embeddings = new LinkedHashMap<>();
        for (Category category : CATEGORIES) {
            System.out.printf("   Embebiendo '%s'...%n", category.name());
            // Combinamos nombre y descripción para que el embedding capture contexto completo
            embeddings.put(category, client.embed(category.name() + ": " + category.description()));
        }
        return embeddings;
    }

    private ClassificationResult classifyText(OllamaClient client, String text,
                                               Map<Category, List<Double>> categoryEmbeddings) {
        var textEmbedding = client.embed(text);

        return categoryEmbeddings.entrySet().stream()
            .map(entry -> new ClassificationResult(
                entry.getKey().name(),
                cosineSimilarity(textEmbedding, entry.getValue())
            ))
            .max(Comparator.comparingDouble(ClassificationResult::similarity))
            .orElseThrow(() -> new IllegalStateException("No hay categorías disponibles"));
    }

    /**
     * Mide el ángulo entre dos vectores, no su magnitud.
     * Resultado en [-1, 1]; valores cercanos a 1 indican máxima similitud semántica.
     */
    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Los vectores deben tener la misma dimensión");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA      += vectorA.get(i) * vectorA.get(i);
            normB      += vectorB.get(i) * vectorB.get(i);
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0.0 ? 0.0 : dotProduct / denominator;
    }
}
