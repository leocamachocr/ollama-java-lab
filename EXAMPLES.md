# Ejemplos — raw-api-module

Cada ejemplo es autocontenido: configura su propio `OllamaClient`, envía la solicitud
y muestra el resultado con contexto descriptivo paso a paso.

| # | Clase | Qué demuestra | Endpoint Ollama |
|---|-------|---------------|-----------------|
| 01 | `Example01_QuestionGeneration` | Solicitar output JSON estructurado con `format: "json"` y deserializarlo en objetos de dominio tipados | `POST /api/chat` |
| 02 | `Example02_EntityExtraction` | Comparar zero-shot, few-shot y chain-of-thought prompting sobre el mismo texto para extracción de entidades | `POST /api/chat` |
| 03 | `Example03_EmbeddingClassification` | Clasificar texto sin etiquetas generando vectores semánticos y buscando la categoría con mayor similitud coseno | `POST /api/embed` |

## Cómo correr

```bash
# Todos los ejemplos en secuencia
./gradlew :raw-api-module:run

# Un ejemplo específico
./gradlew :raw-api-module:run --args="1"   # Generación de preguntas
./gradlew :raw-api-module:run --args="2"   # Extracción de entidades
./gradlew :raw-api-module:run --args="3"   # Clasificación por embeddings
```

**Prerequisito:** Ollama corriendo en `http://localhost:11434` con los modelos:

```bash
ollama pull qwen2.5:7b         # ejemplos 01 y 02
ollama pull nomic-embed-text   # ejemplo 03
```
