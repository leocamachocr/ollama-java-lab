package dev.leocamacho.ollamalab.rawapi.infrastructure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.leocamacho.ollamalab.core.domain.Entity;
import java.util.List;

public class JsonEntityParser {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

    public List<Entity> parse(String json) {
        try {
            var root = objectMapper.readTree(json);
            var entitiesNode = root.get("entities");
            if (entitiesNode == null || !entitiesNode.isArray()) {
                throw new RuntimeException("Response missing 'entities' array. Raw: " + json);
            }
            List<Entity> all = objectMapper.convertValue(
                entitiesNode,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Entity.class)
            );
            return all.stream().filter(e -> e.type() != null).toList();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse entity response. Raw: " + json, e);
        }
    }
}
