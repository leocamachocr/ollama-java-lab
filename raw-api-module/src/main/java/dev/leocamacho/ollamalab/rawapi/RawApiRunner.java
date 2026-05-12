package dev.leocamacho.ollamalab.rawapi;

import dev.leocamacho.ollamalab.rawapi.application.*;
import dev.leocamacho.ollamalab.rawapi.config.ExperimentConfig;
import dev.leocamacho.ollamalab.rawapi.domain.Category;
import dev.leocamacho.ollamalab.rawapi.infrastructure.ExperimentLogger;
import dev.leocamacho.ollamalab.rawapi.infrastructure.JsonEntityParser;
import dev.leocamacho.ollamalab.rawapi.infrastructure.JsonQuestionParser;
import dev.leocamacho.ollamalab.rawapi.infrastructure.OllamaRestAdapter;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RawApiRunner {

    public static void main(String[] args) {
        // Configurar UTF-8 para que las tildes se muestren correctamente en Windows
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setProperty("file.encoding", "UTF-8");



        var config = ExperimentConfig.defaults();
        var adapter = new OllamaRestAdapter(config.ollamaBaseUrl(), config.model(), config.embeddingModel());

        runQuestionGenerator(adapter);
        runEntityExtractor(adapter);
        runEmbeddingClassifier(adapter);
    }

    private static void runQuestionGenerator(OllamaRestAdapter adapter) {
        var useCase = new QuestionGeneratorUseCase(adapter, new JsonQuestionParser());

        ExperimentLogger.logStart("Generación de preguntas", "zero-shot + JSON format");
        var result = useCase.generate(SampleTexts.EDUCATIONAL_TEXT);
        ExperimentLogger.logResult(result);

        System.out.println("  Preguntas parseadas:");
        result.output().forEach(q -> {
            System.out.println("  Q: " + q.text());
            q.options().forEach(o -> System.out.println("     " + o));
            System.out.println("     Correcta: " + q.correct());
            System.out.println();
        });
    }

    private static void runEntityExtractor(OllamaRestAdapter adapter) {
        var useCase = new EntityExtractorUseCase(adapter, new JsonEntityParser());

        for (ExtractionStrategy strategy : ExtractionStrategy.values()) {
            ExperimentLogger.logStart("Extracción de entidades", strategy.name());
            var result = useCase.extract(SampleTexts.TECHNICAL_TEXT, strategy);
            ExperimentLogger.logResult(result);

            System.out.println("  Entidades parseadas:");
            result.output().forEach(e ->
                    System.out.printf("     [%-12s] %s%n", e.type(), e.value())
            );
            System.out.println();
        }
    }

    private static void runEmbeddingClassifier(OllamaRestAdapter adapter) {
        // Definir categorías de ejemplo
        var categoryDefinitions = List.of(
                "tecnología: texto sobre programación, software, lenguajes de programación, frameworks, herramientas de desarrollo o sistemas informáticos",
                "deportes: texto sobre un partido, equipo deportivo, atleta, competición, torneo, resultado deportivo o disciplina deportiva",
                "viajes: texto sobre turismo, destinos, hoteles, vuelos, playas, montañas o experiencias de viaje",
                "gastronomía: texto sobre cocina, recetas, restaurantes, ingredientes, platos típicos o experiencias culinarias"
        );

        System.out.println("📊 Iniciando clasificación por similitud de embeddings...\n");

        // Paso 1: Obtener embeddings de las categorías
        System.out.println("1️⃣  Generando embeddings de categorías...");
        var categories = categoryDefinitions.stream()
                .map(definition -> {
                    String[] parts = definition.split(": ");
                    String categoryName = parts[0];
                    String description = parts[1];

                    long start = System.currentTimeMillis();
                    var embedding = adapter.getEmbedding(definition);
                    long latency = System.currentTimeMillis() - start;

                    System.out.println("   ✓ " + categoryName + " (" + latency + "ms, " + embedding.size() + " dimensiones)");

                    return new Category(categoryName, description, embedding);
                })
                .toList();

        System.out.println("\n2️⃣  Preparando clasificador...");
        var classifier = new TextClassifierUseCase(adapter, categories);

        // Paso 2: Test de clasificación con diferentes textos
        var testTexts = List.of(
                "Java 21 es un lenguaje de programación muy poderoso",
                "El FC Barcelona jugará un partido de fútbol importante",
                "Reservamos un viaje a las playas de Bali",
                "Hicimos una deliciosa paella con mariscos frescos",
                "Python y React son tecnologías populares",
                "Michael Phelps fue un nadador olímpico excepcional"
        );

        System.out.println("\n3️⃣  Clasificando textos...\n");

        for (String text : testTexts) {
            long start = System.currentTimeMillis();
            ClassificationResult result = classifier.classify(text);
            long latency = System.currentTimeMillis() - start;

            System.out.printf(
                    "📝 \"%s\"\n   → Categoría: %s (similitud: %.3f, latencia: %dms)\n",
                    text,
                    result.categoryName(),
                    result.similarity(),
                    latency
            );
        }

        System.out.println("✅ Experimento completado");
    }
}
