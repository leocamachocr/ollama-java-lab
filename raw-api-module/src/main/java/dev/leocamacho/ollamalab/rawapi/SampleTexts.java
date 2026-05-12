package dev.leocamacho.ollamalab.rawapi;

public final class SampleTexts {

    private SampleTexts() {}

    public static final String EDUCATIONAL_TEXT = """
            The Java Virtual Machine (JVM) is a runtime environment that executes Java bytecode.
            Introduced by Sun Microsystems in 1995, the JVM enables Java's "write once, run anywhere"
            philosophy by abstracting hardware and operating system differences.
            The JVM performs three main functions: loading class files, verifying bytecode for safety,
            and executing bytecode through an interpreter or Just-In-Time (JIT) compiler.
            Modern JVMs like HotSpot use adaptive optimization, profiling code at runtime and
            compiling frequently executed "hot" paths into native machine code for maximum performance.
            Garbage collection is another key JVM responsibility, automatically reclaiming memory
            from objects no longer referenced by the program, with algorithms ranging from
            Serial GC for single-threaded applications to G1 GC and ZGC for low-latency workloads.
            """;

    public static final String TECHNICAL_TEXT = """
            Spring Framework, developed by Pivotal (now VMware), provides comprehensive infrastructure
            support for Java enterprise applications. Rod Johnson created it in 2003 as a reaction to
            the complexity of J2EE. The framework's core feature is Dependency Injection (DI),
            which promotes loose coupling through the Inversion of Control (IoC) principle.
            Spring Boot, released in 2014, simplified Spring application development by providing
            auto-configuration and embedded servers like Tomcat. In 2023, Spring Boot 3.x introduced
            native compilation support through GraalVM, enabling instant startup times.
            The Spring ecosystem includes Spring Security for authentication, Spring Data for
            database access, and Spring Cloud for distributed systems patterns.
            """;
}
