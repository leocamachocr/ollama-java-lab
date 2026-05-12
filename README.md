# ollama-java-lab

Laboratorio Java para explorar y documentar las capacidades del modelo `qwen2.5:7b`
corriendo localmente con Ollama. Implementa tres estrategias de integración en módulos
Gradle independientes y compara su ergonomía, rendimiento y flexibilidad.

## Requisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Java | 21 (LTS) | Requiere `JAVA_HOME` configurado |
| Gradle | 8.8 | Ver instrucciones de setup abajo |
| Ollama | Última estable | [ollama.ai](https://ollama.ai) |
| Modelo | qwen2.5:7b | `ollama pull qwen2.5:7b` |

## Setup inicial

```bash
# 1. Descargar el modelo (solo una vez, ~4 GB)
ollama pull qwen2.5:7b

# 2. Iniciar el servidor Ollama (si no está corriendo como servicio)
ollama serve

# 3. Configurar el wrapper de Gradle (solo la primera vez, requiere Gradle instalado)
gradle wrapper

# 4. Verificar que todo compila
./gradlew build -x test
```

En Windows usa `gradlew.bat` en lugar de `./gradlew`.

## Ejecutar experimentos

### raw-api-module (implementado)

```bash
# Correr los dos experimentos (requiere Ollama activo)
./gradlew :raw-api-module:run

# Correr tests de integración (requiere Ollama activo)
./gradlew :raw-api-module:test
```

### Otros módulos (scaffold listo, implementación pendiente)

```bash
./gradlew :spring-ai-module:build
./gradlew :langchain4j-module:build
```

## Estructura del proyecto

```
ollama-java-lab/
├── core/                      # Modelos de dominio compartidos (Question, Entity, ExperimentResult)
├── raw-api-module/            # Ollama REST API directo con java.net.http
│   ├── domain/                # DTOs de la API de Ollama
│   ├── application/           # Casos de uso y puertos
│   ├── infrastructure/        # Adaptadores HTTP y parsers JSON
│   └── config/                # Configuración y wiring
├── spring-ai-module/          # Spring Boot 3 + Spring AI (scaffold)
├── langchain4j-module/        # LangChain4j + Ollama (scaffold)
├── docs/
│   ├── strategies.md          # Técnicas de prompting documentadas
│   ├── comparison.md          # Tabla comparativa de librerías
│   └── experiments/           # Resultados por experimento
└── shared/AGENT_CORE.md       # Contexto para agentes de AI
```

## Experimentos implementados

| # | Nombre | Módulo | Estado |
|---|---|---|---|
| 1 | Generación de preguntas quiz | raw-api-module | Implementado |
| 2 | Extracción de entidades (zero-shot / few-shot / CoT) | raw-api-module | Implementado |
| 3 | Evaluación de calidad y latencia | raw-api-module | Integrado en cada experimento |

## Configuración de Ollama

Por defecto el módulo `raw-api-module` conecta a `http://localhost:11434` con el modelo
`qwen2.5:7b`. Para cambiar estos valores, modifica `ExperimentConfig.defaults()` en
`raw-api-module/src/main/java/.../config/ExperimentConfig.java`.

## Documentación

- [Estrategias de prompting](docs/strategies.md)
- [Comparativa de librerías](docs/comparison.md)
