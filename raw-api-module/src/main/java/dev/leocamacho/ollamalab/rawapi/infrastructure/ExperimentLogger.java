package dev.leocamacho.ollamalab.rawapi.infrastructure;

import dev.leocamacho.ollamalab.core.domain.ExperimentResult;

public final class ExperimentLogger {

    private static final String LINE = "=".repeat(62);

    private ExperimentLogger() {}

    public static void logStart(String experimentName, String strategy) {
        System.out.println("\n" + LINE);
        System.out.printf("  Experimento : %s%n", experimentName);
        System.out.printf("  Estrategia  : %s%n", strategy);
        System.out.println(LINE);
    }

    public static void logResult(ExperimentResult<?> result) {
        System.out.printf("  Latencia    : %dms%n", result.latencyMs());
        System.out.printf("  Tokens est. : ~%d (prompt + eval)%n", result.estimatedTokens());
        System.out.println("  Output:");
        System.out.println(result.rawResponse());
        System.out.println(LINE);
    }

    public static void logError(String message, Exception cause) {
        System.err.println("[ERROR] " + message);
        System.err.println("        " + cause.getMessage());
    }
}
