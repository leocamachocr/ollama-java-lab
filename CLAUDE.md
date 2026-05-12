<!-- ARCHIVO GENERADO — no editar directamente -->
<!-- Editar shared/AGENT_CORE.md y ejecutar: python3 scripts/sync.py -->
<!-- Hash fuente: f6aa3ba4 -->

# AGENT_CORE — ollama-java-lab

## Estado del proyecto
**Proyecto**: ollama-java-lab
**Estado**: iteración 1 — raw-api-module implementado, módulos Spring AI y LangChain4j en scaffold
**Última actualización**: 2026-05-05

## Propósito
Laboratorio Java para experimentar con LLMs locales via Ollama. Objetivo principal: explorar
generación de preguntas educativas y extracción de entidades usando el modelo `qwen2.5:7b`.

## Dominio central
- **Question**: pregunta de opción múltiple (4 opciones A–D, respuesta correcta)
- **Entity**: entidad extraída de texto (PERSON, CONCEPT, TECHNOLOGY, DATE)
- **ExperimentResult<T>**: resultado genérico con output, latencia, tokens y raw response

## Módulos activos
- `raw-api-module`: REST puro, implementado, punto de entrada principal
- `spring-ai-module`: scaffold con dependencias, pendiente de implementación
- `langchain4j-module`: scaffold con dependencias, pendiente de implementación

## Convenciones de código
- Java 21 con records para modelos de dominio
- Arquitectura hexagonal: domain → application → infrastructure
- Sin frameworks en `core/` (solo Jackson para serialización)
- Tests de integración con `Assumptions.assumeTrue` para skip si Ollama no está disponible

## Ollama
- Host: `http://localhost:11434`
- Modelo: `qwen2.5:7b`
- Endpoint: `POST /api/chat` con `format: "json"` para output estructurado
- `stream: false` en todos los experimentos de este lab
