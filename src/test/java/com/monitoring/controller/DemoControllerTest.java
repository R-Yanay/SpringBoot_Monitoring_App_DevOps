package com.monitoring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        mockMvc.perform(post("/api/demo/work"))
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
}
