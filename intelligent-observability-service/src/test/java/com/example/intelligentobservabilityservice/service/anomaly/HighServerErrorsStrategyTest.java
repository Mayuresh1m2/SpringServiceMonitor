package com.example.intelligentobservabilityservice.service.anomaly;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HighServerErrorsStrategyTest {

    private HighServerErrorsStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new HighServerErrorsStrategy(); // Default threshold is 5
    }

    private Metric createErrorMetric(String uri, String status, String exception) {
        Metric metric = new Metric();
        metric.setName("http.server.requests");
        metric.setTags(Map.of("status", status, "uri", uri, "exception", exception));
        metric.setValue(1.0); // Value doesn't matter much for this strategy, count does
        metric.setTimestamp(LocalDateTime.now());
        return metric;
    }

    @Test
    void testDetect_noMetrics_shouldReturnEmpty() {
        Optional<Alert> alert = strategy.detect(new ArrayList<>());
        assertFalse(alert.isPresent());
    }

    @Test
    void testDetect_belowThreshold_shouldReturnEmpty() {
        List<Metric> metrics = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            metrics.add(createErrorMetric("/api/test", "500", "NullPointerException"));
        }
        metrics.add(createErrorMetric("/api/test", "200", "")); // Non-error metric

        Optional<Alert> alert = strategy.detect(metrics);
        assertFalse(alert.isPresent());
    }

    @Test
    void testDetect_atThreshold_shouldGenerateAlert() {
        List<Metric> metrics = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            metrics.add(createErrorMetric("/api/test" + i, "500", "NPE" + i));
        }

        Optional<Alert> optionalAlert = strategy.detect(metrics);
        assertTrue(optionalAlert.isPresent());
        Alert alert = optionalAlert.get();
        assertEquals("HIGH_5XX_ERRORS", alert.getType());
        assertEquals("CRITICAL", alert.getSeverity());
        assertNotNull(alert.getMetricsSnapshot());
        assertEquals(5, alert.getMetricsSnapshot().get("error_metric_count"));
        assertTrue(alert.getRootCause().contains("increase in server-side errors"));
    }

    @Test
    void testDetect_aboveThreshold_shouldGenerateAlert() {
        List<Metric> metrics = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            metrics.add(createErrorMetric("/api/test" + i, "503", "ServiceUnavailable" + i));
        }

        Optional<Alert> optionalAlert = strategy.detect(metrics);
        assertTrue(optionalAlert.isPresent());
        Alert alert = optionalAlert.get();
        assertEquals("HIGH_5XX_ERRORS", alert.getType());
        assertEquals(10, alert.getMetricsSnapshot().get("error_metric_count"));
    }

    @Test
    void testDetect_mixedMetrics_correctlyCountsErrors() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(createErrorMetric("/api/error1", "500", "Error1"));
        metrics.add(createErrorMetric("/api/success1", "200", ""));
        metrics.add(createErrorMetric("/api/error2", "502", "Error2"));
        metrics.add(createErrorMetric("/api/success2", "201", ""));
        metrics.add(createErrorMetric("/api/error3", "500", "Error3"));
        metrics.add(createErrorMetric("/api/error4", "503", "Error4"));
        metrics.add(createErrorMetric("/api/error5", "500", "Error5"));
        // Total 5 error metrics

        Optional<Alert> optionalAlert = strategy.detect(metrics);
        assertTrue(optionalAlert.isPresent());
        Alert alert = optionalAlert.get();
        assertEquals(5, alert.getMetricsSnapshot().get("error_metric_count"));
    }

    @Test
    void testDetect_no5xxMetrics_shouldReturnEmpty() {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(createErrorMetric("/api/success1", "200", ""));
        metrics.add(createErrorMetric("/api/clientError1", "404", "NotFound"));
        metrics.add(createErrorMetric("/api/success2", "201", ""));

        Optional<Alert> alert = strategy.detect(metrics);
        assertFalse(alert.isPresent());
    }

    @Test
    void testGetTargetMetricName() {
        assertEquals("http.server.requests", strategy.getTargetMetricName());
    }

    @Test
    void testGetAnomalyType() {
        assertEquals("HIGH_5XX_ERRORS", strategy.getAnomalyType());
    }
}
