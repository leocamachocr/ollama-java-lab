# Solución para problemas de tildes en Windows

## Problema
En Windows, la terminal (cmd.exe) usa codificación CP1252 por defecto, mientras que Java usa UTF-8. Esto causa que las tildes se muestren como caracteres extraños.

## Solución implementada
Se agregó configuración UTF-8 al inicio de `RawApiRunner.java`:

```java
// Configurar UTF-8 para que las tildes se muestren correctamente en Windows
System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
System.setProperty("file.encoding", "UTF-8");
```

## Verificación
Para probar que funciona, ejecuta:
```bash
java -Dfile.encoding=UTF-8 -cp build/libs/* dev.leocamacho.ollamalab.rawapi.RawApiRunner
```

Deberías ver:
```
  Experimento : Generación de preguntas
  Estrategia  : zero-shot + JSON format
```

En lugar de:
```
  Experimento : Generaci�n de preguntas
  Estrategia  : zero-shot + JSON format
```

## Alternativa si no funciona
Si las tildes siguen sin aparecer, configura la terminal de Windows para usar UTF-8:

1. Abre cmd.exe
2. Ejecuta: `chcp 65001`
3. Luego ejecuta el programa

O usa Windows Terminal/PowerShell que soportan UTF-8 nativamente.
