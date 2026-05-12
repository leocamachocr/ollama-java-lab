package dev.leocamacho.ollamalab.rawapi.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.leocamacho.ollamalab.core.domain.Question;
import java.util.List;

public class JsonQuestionParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Question> parse(String json) {
        try {
            var root = objectMapper.readTree(json);
            var questionsNode = root.get("questions");
            if (questionsNode == null || !questionsNode.isArray()) {
                throw new RuntimeException("Response missing 'questions' array. Raw: " + json);
            }
            return objectMapper.convertValue(
                questionsNode,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Question.class)
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse question response. Raw: " + json, e);
        }
    }
}
