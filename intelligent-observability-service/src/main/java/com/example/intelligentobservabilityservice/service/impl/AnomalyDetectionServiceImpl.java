package com.example.intelligentobservabilityservice.service.impl;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Metric;
import com.example.intelligentobservabilityservice.repository.AlertRepository;
import com.example.intelligentobservabilityservice.repository.MetricRepository;
import com.example.intelligentobservabilityservice.service.AnomalyDetectionService;
import com.example.intelligentobservabilityservice.service.RootCauseSuggestionService;
import com.example.intelligentobservabilityservice.service.anomaly.DetectionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private final List<DetectionStrategy> detectionStrategies; // Spring will inject all beans implementing DetectionStrategy
    private final MetricRepository metricRepository;
    private final AlertRepository alertRepository;
    private final RootCauseSuggestionService rootCauseSuggestionService; // Added

    @Override
    @Transactional
    public List<Alert> runDetectionStrategies(int timeWindowInMinutes) {
        List<Alert> finalAlerts = new ArrayList<>();
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(timeWindowInMinutes);

        log.info("Running anomaly detection strategies for time window: {} to {}", startTime, endTime);

        if (detectionStrategies.isEmpty()) {
            log.warn("No detection strategies found. Anomaly detection will not run.");
            return finalAlerts;
        }

        for (DetectionStrategy strategy : detectionStrategies) {
            try {
                log.debug("Fetching metrics for strategy targeting metric: {}", strategy.getTargetMetricName());
                List<Metric> relevantMetrics = metricRepository.findByNameAndTimestampBetween(
                        strategy.getTargetMetricName(),
                        startTime,
                        endTime
                );

                if (relevantMetrics.isEmpty()) {
                    log.debug("No metrics found for {} in the time window for strategy {}", strategy.getTargetMetricName(), strategy.getClass().getSimpleName());
                    continue;
                }

                log.debug("Running strategy: {} with {} metrics", strategy.getClass().getSimpleName(), relevantMetrics.size());
                Optional<Alert> optionalAlert = strategy.detect(relevantMetrics);

                if (optionalAlert.isPresent()) {
                    Alert alert = optionalAlert.get();
                    // Persist the initial alert
                    Alert savedAlert = alertRepository.save(alert);
                    log.info("Anomaly detected by strategy {}: Alert ID {}, Type: {}",
                            strategy.getClass().getSimpleName(), savedAlert.getId(), savedAlert.getType());

                    // Attempt to add root cause suggestions
                    try {
                        rootCauseSuggestionService.suggestRootCause(savedAlert);
                        // If suggestions were added/modified, save the alert again
                        Alert updatedAlert = alertRepository.save(savedAlert);
                        finalAlerts.add(updatedAlert);
                        log.info("Root cause suggestions added for Alert ID {}", updatedAlert.getId());
                    } catch (Exception rcsEx) {
                        log.error("Error during root cause suggestion for Alert ID {}: {}", savedAlert.getId(), rcsEx.getMessage(), rcsEx);
                        // Add the original alert even if suggestion fails
                        finalAlerts.add(savedAlert);
                    }
                    // Here, one might trigger notifications or further actions with the potentially enriched alert.
                }
            } catch (Exception e) {
                log.error("Error executing detection strategy {}: {}", strategy.getClass().getSimpleName(), e.getMessage(), e);
                // Optionally, create a system alert about the failing strategy
            }
        }

        log.info("Anomaly detection run completed. Finalized {} alerts.", finalAlerts.size());
        return finalAlerts;
    }
}
