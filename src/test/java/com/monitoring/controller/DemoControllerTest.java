package com.monitoring.controller;

import com.monitoring.config.MetricsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MetricsConfig metricsConfig;

    @Test
    void hello_ShouldReturnMessageAndVisits() throws Exception {
        mockMvc.perform(get("/api/demo/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hallo vom Monitoring-Server!"))
                .andExpect(jsonPath("$.visits").exists());
    }

    @Test
    void slowEndpoint_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/demo/slow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dieser Endpunkt ist absichtlich langsam"));
    }

    @Test
    void doWork_ShouldReturnResult() throws Exception {
        mockMvc.perform(get("/api/demo/work"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Arbeit abgeschlossen"))
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    void errorEndpoint_ShouldThrowException() {
        // Der /error Endpunkt wirft eine RuntimeException, die als ServletException gewrappt wird
        org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(get("/api/demo/error"))
        );
    }

    @Test
    void heavyLoad_ShouldReturnResult() throws Exception {
        mockMvc.perform(get("/api/demo/heavy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Schwere Berechnung abgeschlossen"))
                .andExpect(jsonPath("$.cpuResult").exists())
                .andExpect(jsonPath("$.memoryAllocatedMB").value("50"));
    }

    @Test
    void hello_ShouldIncrementCounter() throws Exception {
        long countBefore = metricsConfig.getHelloCounter();
        mockMvc.perform(get("/api/demo/hello"))
                .andExpect(status().isOk());
        long countAfter = metricsConfig.getHelloCounter();
        assertThat(countAfter).isEqualTo(countBefore + 1);
    }

    @Test
    void slowEndpoint_ShouldTakeAtLeastOneSecond() throws Exception {
        long start = System.currentTimeMillis();
        mockMvc.perform(get("/api/demo/slow"))
                .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - start;
        assertThat(duration).isGreaterThanOrEqualTo(900);
    }

    @Test
    void invalidUrl_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/demo/nichtexistierend"))
                .andExpect(status().isNotFound());
    }
}
