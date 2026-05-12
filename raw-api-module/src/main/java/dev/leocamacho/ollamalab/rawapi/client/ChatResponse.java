package dev.leocamacho.ollamalab.rawapi.client;

/**
 * Resultado limpio de una llamada al endpoint /api/chat de Ollama.
 * Abstrae el formato de respuesta interno de la API para que los ejemplos
 * solo accedan a lo que les importa: el texto generado, los tokens y la latencia.
 */
public record ChatResponse(
    String content,           // texto generado por el modelo
    int    promptTokens,      // tokens del prompt enviado
    int    completionTokens,  // tokens generados en la respuesta
    long   latencyMs          // tiempo total de la llamada HTTP en milisegundos
) {
    public int totalTokens() {
        return promptTokens + completionTokens;
    }
}
