package com.example.intelligentobservabilityservice.service.impl;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Metric;
import com.example.intelligentobservabilityservice.repository.MetricRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RootCauseSuggestionServiceImplTest {

    @Mock
    private MetricRepository metricRepository;

    @InjectMocks
    private RootCauseSuggestionServiceImpl rootCauseSuggestionService;

    private Alert testAlert;

    @BeforeEach
    void setUp() {
        testAlert = new Alert();
        testAlert.setId(1L);
        testAlert.setType("HIGH_5XX_ERRORS");
        testAlert.setTimestamp(LocalDateTime.now());
        testAlert.setSuggestions(new ArrayList<>()); // Initialize suggestions list
    }

    private Metric createMetric(String name, double value) {
        Metric metric = new Metric();
        metric.setName(name);
        metric.setValue(value);
        metric.setTimestamp(LocalDateTime.now());
        return metric;
    }

    @Test
    void testSuggestRootCause_notHigh5xxErrorAlert_shouldDoNothing() {
        testAlert.setType("SOME_OTHER_ALERT");
        String originalRootCause = "Original";
        testAlert.setRootCause(originalRootCause);

        rootCauseSuggestionService.suggestRootCause(testAlert);

        assertEquals(originalRootCause, testAlert.getRootCause());
        assertTrue(testAlert.getSuggestions().isEmpty());
    }

    @Test
    void testSuggestRootCause_highCpuUsage() {
        List<Metric> cpuMetrics = List.of(createMetric("system.cpu.usage", 0.9)); // 90% CPU
        List<Metric> memoryMetrics = List.of(createMetric("jvm.memory.used", 100 * 1024 * 1024)); // 100MB

        when(metricRepository.findByNameAndTimestampBetween(eq("system.cpu.usage"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(cpuMetrics);
        when(metricRepository.findByNameAndTimestampBetween(eq("jvm.memory.used"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(memoryMetrics);

        rootCauseSuggestionService.suggestRootCause(testAlert);

        assertNotNull(testAlert.getRootCause());
        assertTrue(testAlert.getRootCause().contains("High CPU utilization (90.00%)"));
        assertFalse(testAlert.getRootCause().contains("High JVM memory usage")); // Memory is not high
        assertEquals(1, testAlert.getSuggestions().size());
        assertTrue(testAlert.getSuggestions().get(0).contains("high CPU"));
    }

    @Test
    void testSuggestRootCause_highMemoryUsage() {
        List<Metric> cpuMetrics = List.of(createMetric("system.cpu.usage", 0.5)); // 50% CPU
        List<Metric> memoryMetrics = List.of(createMetric("jvm.memory.used", 800 * 1024 * 1024)); // 800MB

        when(metricRepository.findByNameAndTimestampBetween(eq("system.cpu.usage"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(cpuMetrics);
        when(metricRepository.findByNameAndTimestampBetween(eq("jvm.memory.used"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(memoryMetrics);

        rootCauseSuggestionService.suggestRootCause(testAlert);

        assertNotNull(testAlert.getRootCause());
        assertTrue(testAlert.getRootCause().contains("High JVM memory usage (" + String.format("%.2f MB", 800.0) + ")"));
        assertFalse(testAlert.getRootCause().contains("High CPU utilization"));
        assertEquals(1, testAlert.getSuggestions().size());
        assertTrue(testAlert.getSuggestions().get(0).contains("memory leaks"));
    }

    @Test
    void testSuggestRootCause_highCpuAndMemoryUsage() {
        List<Metric> cpuMetrics = List.of(createMetric("system.cpu.usage", 0.85)); // 85% CPU
        List<Metric> memoryMetrics = List.of(createMetric("jvm.memory.used", 900 * 1024 * 1024)); // 900MB

        when(metricRepository.findByNameAndTimestampBetween(eq("system.cpu.usage"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(cpuMetrics);
        when(metricRepository.findByNameAndTimestampBetween(eq("jvm.memory.used"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(memoryMetrics);

        rootCauseSuggestionService.suggestRootCause(testAlert);

        assertNotNull(testAlert.getRootCause());
        assertTrue(testAlert.getRootCause().contains("High CPU utilization (85.00%)"));
        assertTrue(testAlert.getRootCause().contains("High JVM memory usage (" + String.format("%.2f MB", 900.0) + ")"));
        assertEquals(2, testAlert.getSuggestions().size()); // One for CPU, one for memory
    }

    @Test
    void testSuggestRootCause_noHighUsage_appendsDefaultMessage() {
        List<Metric> cpuMetrics = List.of(createMetric("system.cpu.usage", 0.1));
        List<Metric> memoryMetrics = List.of(createMetric("jvm.memory.used", 100 * 1024 * 1024));

        when(metricRepository.findByNameAndTimestampBetween(eq("system.cpu.usage"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(cpuMetrics);
        when(metricRepository.findByNameAndTimestampBetween(eq("jvm.memory.used"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(memoryMetrics);

        testAlert.setRootCause("Initial analysis."); // Set some initial root cause

        rootCauseSuggestionService.suggestRootCause(testAlert);

        assertNotNull(testAlert.getRootCause());
        assertTrue(testAlert.getRootCause().startsWith("Initial analysis."));
        assertTrue(testAlert.getRootCause().contains("No obvious resource contention (CPU/Memory) detected"));
        assertTrue(testAlert.getSuggestions().isEmpty()); // No specific suggestions added
    }

    @Test
    void testSuggestRootCause_noRelevantMetricsFound() {
        when(metricRepository.findByNameAndTimestampBetween(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        rootCauseSuggestionService.suggestRootCause(testAlert);

        assertNotNull(testAlert.getRootCause());
        assertTrue(testAlert.getRootCause().contains("No obvious resource contention (CPU/Memory) detected"));
        assertTrue(testAlert.getSuggestions().isEmpty());
    }

    @Test
    void testSuggestRootCause_existingSuggestionsAreKept() {
        testAlert.getSuggestions().add("Existing suggestion 1");
        List<Metric> cpuMetrics = List.of(createMetric("system.cpu.usage", 0.9)); // 90% CPU
        when(metricRepository.findByNameAndTimestampBetween(eq("system.cpu.usage"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(cpuMetrics);
        when(metricRepository.findByNameAndTimestampBetween(eq("jvm.memory.used"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList()); // No memory metrics

        rootCauseSuggestionService.suggestRootCause(testAlert);

        assertEquals(2, testAlert.getSuggestions().size());
        assertEquals("Existing suggestion 1", testAlert.getSuggestions().get(0));
        assertTrue(testAlert.getSuggestions().get(1).contains("high CPU"));
    }
}
