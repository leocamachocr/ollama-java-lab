# Comparativa de librerías — ollama-java-lab

## Resumen

| Criterio | Raw REST API | Spring AI | LangChain4j |
|---|---|---|---|
| Dependencias | Solo Jackson | Spring Boot + Spring AI | langchain4j-core + langchain4j-ollama |
| Curva de aprendizaje | Baja | Media (requiere Spring) | Media |
| Structured output | Manual (JSON parsing) | OutputParser / BeanOutputConverter | OutputParser / JsonSchemaOutputParser |
| Streaming | Manual (SSE parsing) | Flux<ChatResponse> | StreamingResponseHandler |
| Memory (conversación) | Manual | ChatMemory | ChatMemory |
| AiServices declarativos | No | Sí (via ChatClient) | Sí (via @AiService interface) |
| Control fino del prompt | Total | Alto (PromptTemplate) | Alto (PromptTemplate) |
| Testing | JUnit puro | SpringBootTest | JUnit puro |
| Estado de implementación | Implementado | Scaffold | Scaffold |

## Detalle por librería

### Raw REST API (`raw-api-module`)

Llamadas directas a `POST http://localhost:11434/api/chat` usando `java.net.http.HttpClient`
(incluido en Java 11+, sin dependencias extra). Jackson maneja serialización/deserialización.

**Pros:**
- Sin dependencias adicionales más allá de Jackson
- Control total sobre headers, timeout, retry
- Fácil de portar a cualquier proveedor compatible con el protocolo Ollama
- Latencia mínima (sin overhead de framework)

**Contras:**
- Boilerplate manual para conversación con memoria
- Sin abstracciones para streaming
- Parseo de JSON estructurado completamente manual

### Spring AI (`spring-ai-module`)

Spring Boot 3.x + `spring-ai-ollama-spring-boot-starter`. Integración nativa con el
ecosistema Spring (beans, configuración via `application.properties`, Actuator).

**Pros:**
- `ChatClient` fluent API reduce boilerplate
- `BeanOutputConverter` convierte respuestas directamente a POJOs anotados con `@Schema`
- `Flux<ChatResponse>` para streaming reactivo
- `ChatMemory` para conversaciones multiturno
- Integración con Spring Security, Actuator, y observabilidad

**Contras:**
- Requiere todo el stack de Spring Boot
- Tiempo de startup mayor (aunque mitigable con GraalVM native)
- Menos control sobre el request raw

### LangChain4j (`langchain4j-module`)

`langchain4j-ollama` como backend. Arquitectura similar a LangChain (Python) adaptada a Java.

**Pros:**
- `@AiService` declara servicios de AI como interfaces Java — el framework genera la implementación
- `@SystemMessage` y `@UserMessage` en anotaciones
- `ChatMemory` con múltiples estrategias (MessageWindowChatMemory, etc.)
- RAG (Retrieval Augmented Generation) integrado
- Sin dependencia de Spring

**Contras:**
- API cambia frecuentemente entre versiones menores
- Documentación menos madura que Spring AI
- El modelo de anotaciones puede resultar mágico/difícil de depurar
