package com.example.intelligentobservabilityservice.service.impl;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Metric;
import com.example.intelligentobservabilityservice.repository.AlertRepository;
import com.example.intelligentobservabilityservice.repository.MetricRepository;
import com.example.intelligentobservabilityservice.service.RootCauseSuggestionService;
import com.example.intelligentobservabilityservice.service.anomaly.DetectionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceImplTest {

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private RootCauseSuggestionService rootCauseSuggestionService;

    @Mock
    private DetectionStrategy mockStrategy1;

    @Mock
    private DetectionStrategy mockStrategy2;

    @InjectMocks
    private AnomalyDetectionServiceImpl anomalyDetectionService;

    @BeforeEach
    void setUp() {
        // To inject the list of strategies correctly for these tests
        anomalyDetectionService = new AnomalyDetectionServiceImpl(
                List.of(mockStrategy1, mockStrategy2), // Pass the mocked strategies
                metricRepository,
                alertRepository,
                rootCauseSuggestionService
        );
    }

    @Test
    void testRunDetectionStrategies_noStrategies_shouldReturnEmptyList() {
        AnomalyDetectionServiceImpl serviceWithNoStrategies = new AnomalyDetectionServiceImpl(
                Collections.emptyList(), metricRepository, alertRepository, rootCauseSuggestionService
        );
        List<Alert> alerts = serviceWithNoStrategies.runDetectionStrategies(15);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void testRunDetectionStrategies_strategyDetectsAnomaly() {
        Alert detectedAlert = new Alert();
        detectedAlert.setType("MOCK_ANOMALY");
        detectedAlert.setId(1L); // Simulate saved alert

        when(mockStrategy1.getTargetMetricName()).thenReturn("metric1");
        when(metricRepository.findByNameAndTimestampBetween(eq("metric1"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Metric()));
        when(mockStrategy1.detect(anyList())).thenReturn(Optional.of(detectedAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(detectedAlert); // Simulate saving
        doNothing().when(rootCauseSuggestionService).suggestRootCause(any(Alert.class));


        when(mockStrategy2.getTargetMetricName()).thenReturn("metric2"); // Ensure other strategies are called
         when(metricRepository.findByNameAndTimestampBetween(eq("metric2"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList()); // No metrics for strategy 2

        List<Alert> alerts = anomalyDetectionService.runDetectionStrategies(15);

        assertEquals(1, alerts.size());
        assertEquals("MOCK_ANOMALY", alerts.get(0).getType());
        verify(alertRepository, times(2)).save(detectedAlert); // Saved once initially, once after RCS
        verify(rootCauseSuggestionService, times(1)).suggestRootCause(detectedAlert);
        verify(mockStrategy1, times(1)).detect(anyList());
        verify(mockStrategy2, times(1)).detect(anyList()); // detect will not be called if no metrics found
    }

    @Test
    void testRunDetectionStrategies_strategyDetectsAnomaly_RCSUpdatesAlert() {
        Alert initialAlert = new Alert();
        initialAlert.setType("MOCK_ANOMALY");
        initialAlert.setSeverity("LOW");

        Alert savedInitialAlert = new Alert(); // Simulate what's returned by first save
        savedInitialAlert.setId(1L);
        savedInitialAlert.setType("MOCK_ANOMALY");
        savedInitialAlert.setSeverity("LOW");

        Alert alertAfterRCS = new Alert(); // Simulate alert after RCS modifies it
        alertAfterRCS.setId(1L);
        alertAfterRCS.setType("MOCK_ANOMALY");
        alertAfterRCS.setSeverity("HIGH"); // RCS changed severity
        alertAfterRCS.setRootCause("CPU High");


        when(mockStrategy1.getTargetMetricName()).thenReturn("metric1");
        when(metricRepository.findByNameAndTimestampBetween(eq("metric1"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Metric()));
        when(mockStrategy1.detect(anyList())).thenReturn(Optional.of(initialAlert));

        // First save returns savedInitialAlert
        when(alertRepository.save(initialAlert)).thenReturn(savedInitialAlert);

        // RCS service will modify savedInitialAlert
        doAnswer(invocation -> {
            Alert alertArg = invocation.getArgument(0);
            alertArg.setSeverity("HIGH");
            alertArg.setRootCause("CPU High");
            return null;
        }).when(rootCauseSuggestionService).suggestRootCause(savedInitialAlert);

        // Second save (after RCS) returns the modified alert
        when(alertRepository.save(savedInitialAlert)).thenReturn(alertAfterRCS);


        // Strategy 2 finds no metrics
        when(mockStrategy2.getTargetMetricName()).thenReturn("metric2");
        when(metricRepository.findByNameAndTimestampBetween(eq("metric2"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<Alert> alerts = anomalyDetectionService.runDetectionStrategies(15);

        assertEquals(1, alerts.size());
        Alert finalAlert = alerts.get(0);
        assertEquals("MOCK_ANOMALY", finalAlert.getType());
        assertEquals("HIGH", finalAlert.getSeverity()); // Check that RCS change is reflected
        assertEquals("CPU High", finalAlert.getRootCause());

        verify(alertRepository, times(2)).save(any(Alert.class));
        verify(rootCauseSuggestionService, times(1)).suggestRootCause(savedInitialAlert);
    }


    @Test
    void testRunDetectionStrategies_noAnomalyDetected() {
        when(mockStrategy1.getTargetMetricName()).thenReturn("metric1");
        when(metricRepository.findByNameAndTimestampBetween(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Metric()));
        when(mockStrategy1.detect(anyList())).thenReturn(Optional.empty());

        when(mockStrategy2.getTargetMetricName()).thenReturn("metric2");
         when(metricRepository.findByNameAndTimestampBetween(eq("metric2"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Metric()));
        when(mockStrategy2.detect(anyList())).thenReturn(Optional.empty());


        List<Alert> alerts = anomalyDetectionService.runDetectionStrategies(15);
        assertTrue(alerts.isEmpty());
        verify(alertRepository, never()).save(any(Alert.class));
        verify(rootCauseSuggestionService, never()).suggestRootCause(any(Alert.class));
    }

    @Test
    void testRunDetectionStrategies_strategyThrowsException() {
        when(mockStrategy1.getTargetMetricName()).thenReturn("metric1");
        when(metricRepository.findByNameAndTimestampBetween(eq("metric1"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Metric()));
        when(mockStrategy1.detect(anyList())).thenThrow(new RuntimeException("Strategy error"));

        // Strategy 2 behaves normally but detects nothing
        when(mockStrategy2.getTargetMetricName()).thenReturn("metric2");
        when(metricRepository.findByNameAndTimestampBetween(eq("metric2"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Metric()));
        when(mockStrategy2.detect(anyList())).thenReturn(Optional.empty());


        List<Alert> alerts = anomalyDetectionService.runDetectionStrategies(15);
        assertTrue(alerts.isEmpty()); // No alert should be added if strategy fails
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void testRunDetectionStrategies_RCSServiceThrowsException() {
        Alert detectedAlert = new Alert();
        detectedAlert.setType("MOCK_ANOMALY_RCS_FAIL");
        detectedAlert.setId(1L);

        when(mockStrategy1.getTargetMetricName()).thenReturn("metric1");
        when(metricRepository.findByNameAndTimestampBetween(eq("metric1"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Metric()));
        when(mockStrategy1.detect(anyList())).thenReturn(Optional.of(detectedAlert));
        when(alertRepository.save(detectedAlert)).thenReturn(detectedAlert); // Initial save

        // RCS service throws an exception
        doThrow(new RuntimeException("RCS failed")).when(rootCauseSuggestionService).suggestRootCause(detectedAlert);

        // Strategy 2 is not relevant here
        when(mockStrategy2.getTargetMetricName()).thenReturn("metric2");
        when(metricRepository.findByNameAndTimestampBetween(eq("metric2"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());


        List<Alert> alerts = anomalyDetectionService.runDetectionStrategies(15);

        assertEquals(1, alerts.size()); // Alert should still be returned, just not enriched by RCS
        assertEquals(detectedAlert.getType(), alerts.get(0).getType()); // Original alert returned
        verify(alertRepository, times(1)).save(detectedAlert); // Only initial save, second save (post-RCS) is skipped
        verify(rootCauseSuggestionService, times(1)).suggestRootCause(detectedAlert);
    }
}
