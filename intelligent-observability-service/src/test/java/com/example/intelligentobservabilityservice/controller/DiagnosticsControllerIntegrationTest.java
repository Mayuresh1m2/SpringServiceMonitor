package com.example.intelligentobservabilityservice.controller;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Metric;
import com.example.intelligentobservabilityservice.repository.AlertRepository;
import com.example.intelligentobservabilityservice.repository.MetricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DiagnosticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private AlertRepository alertRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        // Clear previous test data
        alertRepository.deleteAll();
        metricRepository.deleteAll();
    }

    private Metric createMetric(String name, Map<String, String> tags, double value, LocalDateTime timestamp) {
        Metric metric = new Metric();
        metric.setName(name);
        metric.setTags(tags);
        metric.setValue(value);
        metric.setTimestamp(timestamp);
        metric.setNamespace("test_namespace");
        metric.setServiceName("test_service");
        return metric;
    }

    @Test
    void testTriggerDiagnostics_noAnomalies() throws Exception {
        // Ingest some non-anomalous metrics
        metricRepository.save(createMetric("http.server.requests", Map.of("status", "200", "uri", "/ok"), 1, LocalDateTime.now().minusMinutes(1)));
        metricRepository.save(createMetric("http.server.requests", Map.of("status", "200", "uri", "/ok"), 1, LocalDateTime.now().minusMinutes(2)));

        mockMvc.perform(post("/api/v1/diagnostics/trigger")
                        .param("timeWindowInMinutes", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Diagnostics run completed. No new anomalies detected."));

        assertEquals(0, alertRepository.count());
    }

    @Test
    void testTriggerDiagnostics_detectsHigh5xxErrors() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        // Ingest metrics that should trigger HighServerErrorsStrategy (default threshold 5)
        for (int i = 0; i < 6; i++) {
            metricRepository.save(createMetric("http.server.requests",
                    Map.of("status", "500", "uri", "/api/error" + i, "exception", "TestException" + i),
                    1, now.minusMinutes(i % 2 + 1) // within last 2 minutes
            ));
        }
        // Ingest some CPU and Memory metrics for root cause suggestion
        metricRepository.save(createMetric("system.cpu.usage", Collections.emptyMap(), 0.9, now.minusMinutes(1))); // High CPU
        metricRepository.save(createMetric("jvm.memory.used", Collections.emptyMap(), 100 * 1024 * 1024, now.minusMinutes(1))); // Normal memory

        mockMvc.perform(post("/api/v1/diagnostics/trigger")
                        .param("timeWindowInMinutes", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("HIGH_5XX_ERRORS")))
                .andExpect(jsonPath("$[0].severity", is("CRITICAL")))
                .andExpect(jsonPath("$[0].metricsSnapshot.error_metric_count", is(6)))
                .andExpect(jsonPath("$[0].rootCause", containsString("High CPU utilization (90.00%)")))
                .andExpect(jsonPath("$[0].suggestions", hasItem(containsString("Investigate processes consuming high CPU"))));


        List<Alert> alerts = alertRepository.findAll();
        assertEquals(1, alerts.size());
        Alert alert = alerts.get(0);
        assertEquals("HIGH_5XX_ERRORS", alert.getType());
        assertTrue(alert.getRootCause().contains("High CPU utilization (90.00%)"));
    }

    @Test
    void testTriggerDiagnostics_invalidTimeWindow() throws Exception {
        mockMvc.perform(post("/api/v1/diagnostics/trigger")
                        .param("timeWindowInMinutes", "0")) // Invalid
                .andExpect(status().isBadRequest())
                .andExpect(content().string("timeWindowInMinutes must be positive."));
    }
}
