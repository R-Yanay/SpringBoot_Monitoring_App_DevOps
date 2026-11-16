package com.monitoring.controller;

import com.monitoring.config.MetricsConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final AtomicLong visitCounter = new AtomicLong(0);
    private final MetricsConfig metricsConfig;

    public DemoController(MetricsConfig metricsConfig) {
        this.metricsConfig = metricsConfig;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        long visits = visitCounter.incrementAndGet();
        metricsConfig.incrementHelloCounter();
        return Map.of(
                "message", "Hallo vom Monitoring-Server!",
                "visits", visits
        );
    }

    @GetMapping("/slow")
    public Map<String, String> slowEndpoint() throws InterruptedException {
        metricsConfig.incrementSlowCounter();
        Timer.builder("app.custom.timer")
                .description("Benutzerdefinierter Timer für Operationen")
                .register(metricsConfig.getRegistry())
                .record(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
        return Map.of("message", "Dieser Endpunkt ist absichtlich langsam");
    }

    @GetMapping("/work")
    public Map<String, String> doWork() {
        metricsConfig.incrementWorkCounter();
        long result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += i;
        }
        return Map.of("message", "Arbeit abgeschlossen", "result", String.valueOf(result));
    }

    @GetMapping("/error")
    public Map<String, String> errorEndpoint() {
        metricsConfig.incrementErrorCounter();
        throw new RuntimeException("Absichtlicher Fehler für Metriken-Tests");
    }

    @GetMapping("/heavy")
    public Map<String, String> heavyLoad() throws InterruptedException {
        metricsConfig.incrementHeavyCounter();
        // CPU-Last: 10 Millionen Berechnungen
        long cpuResult = 0;
        for (int i = 0; i < 10000000; i++) {
            cpuResult += Math.sqrt(i) * Math.sin(i);
        }

        // Memory-Last: 50 MB allozieren und halten
        byte[] memoryLoad = new byte[50 * 1024 * 1024];

        return Map.of(
                "message", "Schwere Berechnung abgeschlossen",
                "cpuResult", String.valueOf(cpuResult),
                "memoryAllocatedMB", "50"
        );
    }
}
