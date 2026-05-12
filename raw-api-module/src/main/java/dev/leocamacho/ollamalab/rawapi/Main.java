package dev.leocamacho.ollamalab.rawapi;

import dev.leocamacho.ollamalab.rawapi.examples.Example01_QuestionGeneration;
import dev.leocamacho.ollamalab.rawapi.examples.Example02_EntityExtraction;
import dev.leocamacho.ollamalab.rawapi.examples.Example03_EmbeddingClassification;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Punto de entrada del módulo raw-api-module.
 *
 * Sin argumentos corre los tres ejemplos en secuencia.
 * Con un argumento numérico (1, 2 o 3) corre solo ese ejemplo.
 *
 * Uso:
 *   ./gradlew :raw-api-module:run              — todos los ejemplos
 *   ./gradlew :raw-api-module:run --args="1"   — solo Ejemplo 01
 *   ./gradlew :raw-api-module:run --args="2"   — solo Ejemplo 02
 *   ./gradlew :raw-api-module:run --args="3"   — solo Ejemplo 03
 */
public class Main {

    public static void main(String[] args) {
        // UTF-8 para que las tildes se muestren correctamente en Windows
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        int selectedExample = args.length > 0 ? Integer.parseInt(args[0]) : 0;

        switch (selectedExample) {
            case 1 -> new Example01_QuestionGeneration().run();
            case 2 -> new Example02_EntityExtraction().run();
            case 3 -> new Example03_EmbeddingClassification().run();
            default -> {
                System.out.println("Corriendo todos los ejemplos (pasa 1, 2 o 3 para elegir uno).\n");
                new Example01_QuestionGeneration().run();
                System.out.println("\n");
                new Example02_EntityExtraction().run();
                System.out.println("\n");
                new Example03_EmbeddingClassification().run();
            }
        }
    }
}
