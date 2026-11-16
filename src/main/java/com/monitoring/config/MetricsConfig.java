package com.monitoring.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class MetricsConfig {

    private final AtomicLong customCounter = new AtomicLong(0);
    private final MeterRegistry registry;

    // Separate Counter für jede URL
    private final AtomicLong helloCounter = new AtomicLong(0);
    private final AtomicLong slowCounter = new AtomicLong(0);
    private final AtomicLong workCounter = new AtomicLong(0);
    private final AtomicLong errorCounter = new AtomicLong(0);
    private final AtomicLong heavyCounter = new AtomicLong(0);

    public MetricsConfig(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void initCustomMetrics() {
        // Counter: Zählt die Anzahl der benutzerdefinierten Ereignisse
        Counter.builder("app.custom.events")
                .description("Benutzerdefinierte Ereignisse in der Anwendung")
                .register(registry);

        // Gauge: Misst den aktuellen Wert eines Zählers
        Gauge.builder("app.custom.gauge", customCounter, AtomicLong::get)
                .description("Benutzerdefinierter Gauge-Wert")
                .register(registry);

        // Timer: Misst die Dauer von Operationen
        Timer.builder("app.custom.timer")
                .description("Benutzerdefinierter Timer für Operationen")
                .register(registry);

        // Separate Counter für jede URL
        Counter.builder("app.hello.calls")
                .description("Anzahl der Aufrufe von /api/demo/hello")
                .register(registry);

        Counter.builder("app.slow.calls")
                .description("Anzahl der Aufrufe von /api/demo/slow")
                .register(registry);

        Counter.builder("app.work.calls")
                .description("Anzahl der Aufrufe von /api/demo/work")
                .register(registry);

        Counter.builder("app.error.calls")
                .description("Anzahl der Aufrufe von /api/demo/error")
                .register(registry);

        Counter.builder("app.heavy.calls")
                .description("Anzahl der Aufrufe von /api/demo/heavy")
                .register(registry);

        // Separate Gauges für jede URL
        Gauge.builder("app.hello.count", helloCounter, AtomicLong::get)
                .description("Zähler für /hello Aufrufe")
                .register(registry);

        Gauge.builder("app.slow.count", slowCounter, AtomicLong::get)
                .description("Zähler für /slow Aufrufe")
                .register(registry);

        Gauge.builder("app.work.count", workCounter, AtomicLong::get)
                .description("Zähler für /work Aufrufe")
                .register(registry);

        Gauge.builder("app.error.count", errorCounter, AtomicLong::get)
                .description("Zähler für /error Aufrufe")
                .register(registry);

        Gauge.builder("app.heavy.count", heavyCounter, AtomicLong::get)
                .description("Zähler für /heavy Aufrufe")
                .register(registry);
    }

    public void incrementCustomCounter() {
        customCounter.incrementAndGet();
    }

    public long getCustomCounter() {
        return customCounter.get();
    }

    // Separate Counter-Methoden für jede URL
    public void incrementHelloCounter() {
        helloCounter.incrementAndGet();
        Counter.builder("app.hello.calls")
                .description("Anzahl der Aufrufe von /api/demo/hello")
                .register(registry)
                .increment();
    }

    public void incrementSlowCounter() {
        slowCounter.incrementAndGet();
        Counter.builder("app.slow.calls")
                .description("Anzahl der Aufrufe von /api/demo/slow")
                .register(registry)
                .increment();
    }

    public void incrementWorkCounter() {
        workCounter.incrementAndGet();
        Counter.builder("app.work.calls")
                .description("Anzahl der Aufrufe von /api/demo/work")
                .register(registry)
                .increment();
    }

    public void incrementErrorCounter() {
        errorCounter.incrementAndGet();
        Counter.builder("app.error.calls")
                .description("Anzahl der Aufrufe von /api/demo/error")
                .register(registry)
                .increment();
    }

    public void incrementHeavyCounter() {
        heavyCounter.incrementAndGet();
        Counter.builder("app.heavy.calls")
                .description("Anzahl der Aufrufe von /api/demo/heavy")
                .register(registry)
                .increment();
    }

    public long getHelloCounter() { return helloCounter.get(); }
    public long getSlowCounter() { return slowCounter.get(); }
    public long getWorkCounter() { return workCounter.get(); }
    public long getErrorCounter() { return errorCounter.get(); }
    public long getHeavyCounter() { return heavyCounter.get(); }

    public MeterRegistry getRegistry() {
        return registry;
    }
}
