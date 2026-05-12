# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Propósito
Laboratorio Java para experimentar con LLMs locales via Ollama. Objetivo principal: explorar
generación de preguntas educativas, extracción de entidades y clasificación por embeddings
usando los modelos `qwen2.5:7b` (chat) y `nomic-embed-text` (embeddings).

## Comandos frecuentes

```bash
# Compilar todo el proyecto
./gradlew build

# Ejecutar el runner principal (requiere Ollama corriendo en localhost:11434)
./gradlew :raw-api-module:run

# Correr todos los tests (los de integración se saltan automáticamente si Ollama no está disponible)
./gradlew test

# Correr un test específico
./gradlew :raw-api-module:test --tests "dev.leocamacho.ollamalab.rawapi.QuestionGeneratorIntegrationTest"

# Compilar sin tests
./gradlew assemble
```

## Arquitectura

El proyecto es un monorepo Gradle con 4 módulos:

- **`core`**: biblioteca sin framework. Define los records de dominio compartidos (`Question`, `Entity`, `EntityType`, `ExperimentResult<T>`) y depende solo de Jackson.
- **`raw-api-module`**: implementación completa usando `java.net.http.HttpClient` puro. Punto de entrada: `RawApiRunner.main()`.
- **`spring-ai-module`**: scaffold pendiente de implementación.
- **`langchain4j-module`**: scaffold pendiente de implementación.

### Flujo dentro de `raw-api-module`

```
Main
    └── Example0N.run()
            └── OllamaClient          ← toda la HTTP + JSON (de)serialización
                    ├── chat()        → ChatResponse  (content, tokens, latencyMs)
                    └── embed()       → List<Double>  (vector semántico)
```

Los `model/` records son los DTOs internos que usa `OllamaClient`; los ejemplos nunca los ven.

### Ejemplos implementados

| # | Clase | Endpoint |
|---|-------|----------|
| 01 | `Example01_QuestionGeneration` — JSON estructurado con `format: "json"` | `/api/chat` |
| 02 | `Example02_EntityExtraction` — zero-shot vs few-shot vs chain-of-thought | `/api/chat` |
| 03 | `Example03_EmbeddingClassification` — similitud coseno sobre vectores | `/api/embed` |

`ExtractionStrategy` es un enum que lleva su propio system prompt; no hay switch externo.

## Dominio central

- **`Question`** (core): pregunta de opción múltiple — 4 opciones A–D, campo `correct`
- **`Entity`** (core): entidad extraída — `value` + `EntityType` (PERSON, CONCEPT, TECHNOLOGY, DATE)
- **`ExperimentResult<T>`** (core): wrapper genérico para todos los experimentos
- **`Category`** (raw-api-module): nombre + descripción + vector de embedding precalculado

## Convenciones de código

- Java 21 con records para todos los modelos de dominio
- Arquitectura hexagonal: `domain` → `application` → `infrastructure`; `core/` sin frameworks
- Tests de integración: `@BeforeAll` con `Assumptions.assumeTrue(isOllamaAvailable())` — se skipean si Ollama no responde

## Ollama

- Host: `http://localhost:11434` (configurable via `ExperimentConfig`)
- Modelo chat: `qwen2.5:7b` | Modelo embeddings: `nomic-embed-text`
- Todos los requests usan `stream: false` y `format: "json"`
- Timeout HTTP: 120s por request (los modelos locales son lentos)
