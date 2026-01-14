package com.example.intelligentobservabilityservice.service.anomaly;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Metric;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component // Marks this as a Spring bean to be discovered
public class HighServerErrorsStrategy implements DetectionStrategy {

    private static final String TARGET_METRIC = "http.server.requests";
    private static final String STATUS_TAG_KEY = "status";
    private static final String URI_TAG_KEY = "uri";
    private static final String EXCEPTION_TAG_KEY = "exception";
    private static final int ERROR_THRESHOLD = 5; // Trigger alert if 5 or more 5xx errors are found

    @Override
    public Optional<Alert> detect(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return Optional.empty();
        }

        List<Metric> serverErrorMetrics = metrics.stream()
                .filter(metric -> TARGET_METRIC.equals(metric.getName()))
                .filter(metric -> {
                    Map<String, String> tags = metric.getTags();
                    return tags != null && tags.containsKey(STATUS_TAG_KEY) && tags.get(STATUS_TAG_KEY).startsWith("5");
                })
                .collect(Collectors.toList());

        if (serverErrorMetrics.size() >= ERROR_THRESHOLD) {
            Alert alert = new Alert();
            alert.setTimestamp(LocalDateTime.now());
            alert.setType(getAnomalyType());
            alert.setSeverity("CRITICAL");

            // Create a simple snapshot
            Map<String, Object> snapshot = Map.of(
                    "description", "High number of 5xx server errors detected.",
                    "error_metric_count", serverErrorMetrics.size(),
                    "threshold", ERROR_THRESHOLD,
                    "sample_error_uris", serverErrorMetrics.stream()
                                            .map(m -> m.getTags().getOrDefault(URI_TAG_KEY, "unknown_uri"))
                                            .distinct()
                                            .limit(5)
                                            .collect(Collectors.toList()),
                    "sample_exceptions", serverErrorMetrics.stream()
                                            .map(m -> m.getTags().getOrDefault(EXCEPTION_TAG_KEY, "unknown_exception"))
                                            .distinct()
                                            .limit(5)
                                            .collect(Collectors.toList())
            );
            alert.setMetricsSnapshot(snapshot);
            alert.setRootCause("Initial analysis suggests an increase in server-side errors (5xx)."); // Placeholder
            alert.setSuggestions(Collections.singletonList("Investigate application logs for corresponding 5xx errors. Check recent deployments or changes.")); // Placeholder

            return Optional.of(alert);
        }

        return Optional.empty();
    }

    @Override
    public String getTargetMetricName() {
        return TARGET_METRIC;
    }

    @Override
    public String getAnomalyType() {
        return "HIGH_5XX_ERRORS";
    }
}
