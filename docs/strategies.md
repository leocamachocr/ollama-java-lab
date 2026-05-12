# Estrategias de prompting — ollama-java-lab

## Modelo: qwen2.5:7b

**Familia**: Qwen 2.5 (Alibaba Cloud)  
**Parámetros**: 7 billones  
**Contexto**: 128K tokens  
**Cuantización**: Q4_K_M (por defecto en Ollama, ~4.4 GB en disco)

### Perfil del modelo

`qwen2.5:7b` destaca entre los modelos 7B por su rendimiento en razonamiento estructurado
y seguimiento de instrucciones. Es especialmente capaz para:

- Generación de contenido estructurado (JSON, XML, Markdown)
- Extracción de información desde texto
- Clasificación y categorización
- Generación de preguntas educativas

**Limitaciones observadas en 7B:**
- Alucinaciones ocasionales en detalles específicos (fechas, números)
- Puede ignorar restricciones de cantidad ("exactamente 5") si el prompt no es explícito
- El formato JSON puede incluir trailing commas o campos extra si el prompt no especifica "ONLY"

---

## Configuración base (raw-api-module)

```json
{
  "model": "qwen2.5:7b",
  "stream": false,
  "format": "json",
  "messages": [
    { "role": "system", "content": "<system_prompt>" },
    { "role": "user",   "content": "<input_text>" }
  ]
}
```

El parámetro `format: "json"` le indica a Ollama que fuerce JSON válido en la respuesta.
**No garantiza la estructura**, solo que el output sea JSON parseable.

---

## Técnicas documentadas

### 1. Zero-shot prompting

**Descripción**: Instrucción directa sin ejemplos. El modelo infiere la tarea desde la descripción.

**Cuándo usar**: Tareas simples y bien definidas con un schema claro.

**Resultado observado**: Funciona bien para generación de preguntas cuando el schema
está en el prompt. Para extracción de entidades, tiende a omitir entidades implícitas.

**Ejemplo de prompt efectivo**:
```
You are an educational assessment expert. Generate exactly 5 multiple-choice quiz questions.

Rules:
- Each question must test a specific fact from the text
- Each question has exactly 4 options labeled A, B, C, D
- Exactly one option is correct
- The correct field must be a single letter: A, B, C, or D

Return ONLY a valid JSON object. No explanation, no markdown, no extra text.
```

**Clave**: La frase `Return ONLY a valid JSON object. No explanation, no markdown, no extra text.`
reduce drásticamente la probabilidad de que el modelo agregue texto fuera del JSON.

---

### 2. Few-shot prompting

**Descripción**: El prompt incluye 2–3 ejemplos de input → output antes de la tarea real.

**Cuándo usar**: Cuando el formato de salida es complejo o el modelo tiende a variar
la estructura. Mejora consistencia a costa de tokens.

**Ventaja principal**: El modelo "aprende" el formato exacto desde los ejemplos en lugar
de inferirlo desde la descripción.

**Trade-off**: Aumenta el costo de tokens del prompt en ~30–50%. En un modelo 7B local
el costo es tiempo de CPU/GPU, no monetario.

**Ejemplo aplicado a extracción de entidades**:
```
Extract entities. Examples:

Text: "Java 21 was released by Oracle in September 2023."
Output: {"entities":[{"value":"Java 21","type":"TECHNOLOGY"},{"value":"Oracle","type":"PERSON"},{"value":"September 2023","type":"DATE"}]}

Text: "The Transformer architecture enables attention mechanisms."
Output: {"entities":[{"value":"Transformer","type":"TECHNOLOGY"},{"value":"attention mechanisms","type":"CONCEPT"}]}

Now extract entities from this text. Return ONLY valid JSON:
```

---

### 3. Chain-of-Thought (CoT)

**Descripción**: El prompt guía al modelo a razonar paso a paso antes de producir la
respuesta final. Útil para tareas de clasificación donde el contexto importa.

**Cuándo usar**: Cuando la tarea requiere desambiguación (¿"Spring" es tecnología o
concepto?), o cuando el zero-shot produce categorías incorrectas.

**Limitación en modelos 7B**: El CoT puede aumentar la latencia 2–3x y el modelo puede
"olvidarse" de producir JSON al final del razonamiento si el prompt no es suficientemente
explícito.

**Mitigación**: Separar el razonamiento del output con instrucción explícita:
```
Step 1–4: [razonamiento...]
Step 5: Return ONLY the following JSON with no other text:
{"entities": [...]}
```

---

## Manejo de respuestas que no cumplen el schema

### Estrategia actual (raw-api-module)

Se lanza `RuntimeException` con el raw response incluido en el mensaje. El caller decide
si reintentar o abortar.

### Estrategia recomendada para producción

1. **Retry con backoff**: máximo 3 intentos, el segundo intento incluye el error de parsing
   como contexto en el prompt ("Your previous response was not valid JSON: ...")
2. **Parse fallback**: intentar extraer el JSON con regex si el modelo incluyó texto antes/después
3. **Schema validation**: usar Jackson con modo `FAIL_ON_UNKNOWN_PROPERTIES = false` para
   ser tolerante con campos extra

---

## Métricas de latencia observadas (hardware de referencia)

| Escenario | Latencia típica |
|---|---|
| Primera llamada (modelo en frío) | 8–15 segundos |
| Llamadas subsecuentes (modelo en caliente) | 3–8 segundos |
| Prompt largo (>500 tokens) | 10–20 segundos |

*Medidas en CPU sin GPU. Con GPU dedicada, reducir por 5–10x.*
