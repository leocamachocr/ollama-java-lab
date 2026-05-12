package dev.leocamacho.ollamalab.rawapi.application;

import dev.leocamacho.ollamalab.core.domain.ExperimentResult;
import dev.leocamacho.ollamalab.core.domain.Question;
import dev.leocamacho.ollamalab.rawapi.infrastructure.JsonQuestionParser;
import java.util.List;

public class QuestionGeneratorUseCase {

    private static final String SYSTEM_PROMPT = """
            You are an educational assessment expert. Generate exactly 5 multiple-choice quiz questions based on the provided text.

            Rules:
            - Each question must test a specific fact from the text
            - Each question has exactly 4 options labeled A, B, C, D
            - Exactly one option is correct
            - Options must be plausible and similar in length
            - The correct field must be a single letter: A, B, C, or D

            Return ONLY a valid JSON object. No explanation, no markdown, no extra text.

            Required format:
            {"questions":[{"text":"Question?","options":["A) ...","B) ...","C) ...","D) ..."],"correct":"A"}]}
            """;
    public static final String ALTERNATIVE= """
            ### ROLE: Educational Expert
            ### TASK: Generate 5 MCQ from the provided text
            ### RULES:
            1. One fact per question.
            2. 4 plausible options (A-D).
            3. Field "correct" must be A, B, C, or D.
            4. Output: STRICT JSON ONLY.
            
            ### SCHEMA:
            {"questions":[{"text":string,"options":[string,string,string,string],"correct":char}]}
            
            ### [TEXT]:
            {input_text}
            """;

    private final LlmPort llmPort;
    private final JsonQuestionParser parser;

    public QuestionGeneratorUseCase(LlmPort llmPort, JsonQuestionParser parser) {
        this.llmPort = llmPort;
        this.parser = parser;
    }

    public ExperimentResult<List<Question>> generate(String inputText) {
        long start = System.currentTimeMillis();
        var response = llmPort.chat(SYSTEM_PROMPT, inputText);
        long latencyMs = System.currentTimeMillis() - start;

        var questions = parser.parse(response.message().content());
        int estimatedTokens = response.promptEvalCount() + response.evalCount();

        return new ExperimentResult<>(questions, latencyMs, response.message().content(), estimatedTokens);
    }
}
