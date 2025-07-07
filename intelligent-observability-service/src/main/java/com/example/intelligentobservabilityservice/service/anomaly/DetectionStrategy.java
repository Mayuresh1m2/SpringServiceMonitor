package com.example.intelligentobservabilityservice.service.anomaly;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Metric;

import java.util.List;
import java.util.Optional;

/**
 * Interface for different anomaly detection strategies.
 */
public interface DetectionStrategy {

    /**
     * Analyzes a list of metrics and returns an Optional Alert if an anomaly is detected.
     *
     * @param metrics The list of metrics to analyze. Typically, these would be metrics
     *                from a specific time window or a specific type.
     * @return An Optional containing an Alert if an anomaly is detected, otherwise an empty Optional.
     */
    Optional<Alert> detect(List<Metric> metrics);

    /**
     * Returns the name of the specific metric this strategy is interested in.
     * This helps the AnomalyDetectionService to fetch relevant metrics for this strategy.
     * For example, "http.server.requests".
     */
    String getTargetMetricName();

    /**
     * Returns the type of alert this strategy generates, e.g., "HIGH_5XX_ERRORS".
     */
    String getAnomalyType();

}
