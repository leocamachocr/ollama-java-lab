# Clasificación de Texto basada en Embeddings

## 📋 Descripción

Este ejemplo demuestra cómo usar embeddings (vectores) para clasificar texto sin enviar prompts al modelo. 

**Flujo:**
1. Se generan embeddings para cada categoría (solo una vez)
2. Se almacenan los vectores
3. Para clasificar un nuevo texto, se obtiene su embedding
4. Se calcula similitud coseno entre el vector del texto y cada categoría
5. Se retorna la categoría más similar

## 🔧 Componentes

### `OllamaEmbeddingRequest` / `OllamaEmbeddingResponse`
- Records para manejar la API `/api/embed` de Ollama
- Convierte texto en vectores de números reales

### `LlmPort` (extendido)
```java
List<Double> getEmbedding(String text);
```
- Nuevo método en la interfaz para obtener embeddings

### `OllamaRestAdapter` (extendido)
```java
public List<Double> getEmbedding(String text)
```
- Implementación HTTP que llama a `/api/embed` de Ollama
- Retorna una `List<Double>` con las dimensiones del vector

### `SimilarityCalculator`
```java
static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB)
```
- Calcula la similitud coseno entre dos vectores
- Rango: -1.0 a 1.0 (1.0 = idénticos, 0.0 = ortogonales, -1.0 = opuestos)

### `TextClassifierUseCase`
```java
ClassificationResult classify(String text)
```
- Recibe un texto
- Obtiene su embedding
- Compara con todas las categorías
- Retorna la más similar

### `Category`
```java
record Category(String name, String description, List<Double> embedding)
```
- Representa una clase con su descripción y vector pre-calculado

### `ClassificationResult`
```java
record ClassificationResult(String categoryName, double similarity, String description)
```
- Resultado de la clasificación con similitud (0-1)

## 🚀 Cómo ejecutar

### 1. Preparar categorías con embeddings
```java
List<String> categoryDefinitions = List.of(
    "tecnología: lenguajes de programación, frameworks, herramientas",
    "deportes: fútbol, tenis, natación, atletismo",
    "viajes: turismo, hoteles, playas, destinos"
);

List<Category> categories = categoryDefinitions.stream()
    .map(def -> {
        String[] parts = def.split(": ");
        var embedding = adapter.getEmbedding(def);
        return new Category(parts[0], parts[1], embedding);
    })
    .toList();
```

### 2. Crear clasificador
```java
var classifier = new TextClassifierUseCase(adapter, categories);
```

### 3. Clasificar textos
```java
ClassificationResult result = classifier.classify(
    "Java 21 es un lenguaje de programación muy poderoso"
);
System.out.println(result.categoryName());    // "tecnología"
System.out.println(result.similarity());      // 0.87 (ejemplo)
```

## 📊 Ventajas vs. Prompting

| Aspecto | Embeddings | Prompting |
|--------|-----------|----------|
| **Velocidad** | ⚡ Muy rápido (solo similitud) | 🐢 Lento (genera tokens) |
| **Costo** | 💰 Bajo (sin tokens) | 💸 Alto (token generation) |
| **Precisión** | 👍 Alta para categorización | 👍 Alta pero variable |
| **Escalabilidad** | 📈 Excelente con many categories | 📉 Decrece con categorías |
| **Latencia** | <10ms por clasificación | >1000ms por chat |

## 💡 Casos de uso

- **Clasificación de documentos**
- **Routing de tickets**
- **Búsqueda semántica**
- **Detección de similitud**
- **Clustering de textos**

## 📝 Notas técnicas

- Los embeddings tienen entre 384 y 4096 dimensiones dependiendo del modelo
- La similitud coseno es robusta porque es invariante al escala
- Para producción, guardar embeddings en BD vectorial (Pinecone, Weaviate, etc.)
- El modelo `qwen2.5:7b` soporta `/api/embed` directamente

## 🔗 Referencia

Archivo de ejemplo: `EmbeddingClassifierRunner.java`

