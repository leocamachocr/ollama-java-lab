package dev.leocamacho.ollamalab.rawapi.examples;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.leocamacho.ollamalab.core.domain.Entity;
import dev.leocamacho.ollamalab.rawapi.SampleTexts;
import dev.leocamacho.ollamalab.rawapi.client.OllamaClient;
import java.util.List;

/**
 * Ejemplo 02 — Extracción de entidades comparando tres estrategias de prompting.
 *
 * Aplica zero-shot, few-shot y chain-of-thought sobre el mismo texto técnico
 * para mostrar cómo la estrategia de prompting afecta la calidad y cantidad
 * de entidades extraídas sin cambiar el modelo ni el texto de entrada.
 */
public class Example02_EntityExtraction {

    /** Ejecuta las tres estrategias en secuencia y muestra los resultados comparados. */
    public void run() {
        var client = OllamaClient.withDefaults();
        System.out.println("═══ Ejemplo 02: Extracción de entidades — comparación de estrategias ═══\n");
        System.out.println("→ Texto de entrada: párrafo sobre el Spring Framework\n");

        for (ExtractionStrategy strategy : ExtractionStrategy.values()) {
            System.out.println("─── Estrategia: " + strategy.name() + " ───");
            System.out.println("→ Enviando prompt al modelo...");

            var chatResponse = client.chat(strategy.systemPrompt(), SampleTexts.TECHNICAL_TEXT);
            var entities = parseEntities(chatResponse.content());

            System.out.printf("← %dms | ~%d tokens | %d entidades encontradas%n%n",
                chatResponse.latencyMs(), chatResponse.totalTokens(), entities.size());

            entities.forEach(entity ->
                System.out.printf("   [%-12s] %s%n", entity.type(), entity.value())
            );
            System.out.println();
        }
    }

    /** Extrae entidades con la estrategia indicada. Expuesto para los tests de integración. */
    public List<Entity> extractEntities(String inputText, ExtractionStrategy strategy) {
        var chatResponse = OllamaClient.withDefaults().chat(strategy.systemPrompt(), inputText);
        return parseEntities(chatResponse.content());
    }

    private List<Entity> parseEntities(String jsonContent) {
        try {
            // READ_UNKNOWN_ENUM_VALUES_AS_NULL: si el modelo devuelve un tipo no reconocido,
            // lo mapeamos a null en lugar de lanzar una excepción, y luego lo filtramos
            var mapper = new ObjectMapper()
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

            var entitiesNode = mapper.readTree(jsonContent).get("entities");
            if (entitiesNode == null || !entitiesNode.isArray()) {
                throw new RuntimeException("Response missing 'entities' array. Raw: " + jsonContent);
            }

            List<Entity> allEntities = mapper.convertValue(
                entitiesNode,
                mapper.getTypeFactory().constructCollectionType(List.class, Entity.class)
            );

            return allEntities.stream().filter(entity -> entity.type() != null).toList();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse entity JSON", e);
        }
    }
}
