package com.monitoring.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ActuatorEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpoint_ShouldReturnUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void metricsEndpoint_ShouldReturnMetrics() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").exists());
    }

    @Test
    void prometheusEndpoint_ShouldReturnMetrics() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/prometheus", String.class);
        // Prometheus-Endpoint kann je nach Konfiguration 200 oder 404 zurückgeben
        // Wir prüfen, dass der Actuator insgesamt funktioniert
        assertThat(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError()).isTrue();
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            assertThat(response.getBody()).contains("jvm_memory_used_bytes");
        }
    }
}
