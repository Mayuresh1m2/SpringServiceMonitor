package com.example.intelligentobservabilityservice.service.impl;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Metric;
import com.example.intelligentobservabilityservice.repository.MetricRepository;
import com.example.intelligentobservabilityservice.service.RootCauseSuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
@Slf4j
public class RootCauseSuggestionServiceImpl implements RootCauseSuggestionService {

    private final MetricRepository metricRepository;

    private static final String HIGH_5XX_ALERT_TYPE = "HIGH_5XX_ERRORS";
    private static final String CPU_USAGE_METRIC_NAME = "system.cpu.usage";
    private static final String MEMORY_USAGE_METRIC_NAME = "jvm.memory.used"; // Assuming this is in bytes

    private static final double HIGH_CPU_THRESHOLD = 0.8; // 80% CPU usage
    private static final double HIGH_MEMORY_THRESHOLD_BYTES = 1024 * 1024 * 768; // 768 MB, example

    @Override
    public void suggestRootCause(Alert alert) {
        if (alert == null || !HIGH_5XX_ALERT_TYPE.equals(alert.getType())) {
            log.debug("Skipping root cause suggestion for alert: {} as it's not a {} alert type or is null.",
                    alert == null ? "null" : alert.getId(), HIGH_5XX_ALERT_TYPE);
            return;
        }

        log.info("Attempting to suggest root cause for alert ID: {}", alert.getId());

        LocalDateTime alertTimestamp = alert.getTimestamp();
        LocalDateTime startTime = alertTimestamp.minusMinutes(5);
        LocalDateTime endTime = alertTimestamp.plusMinutes(5); // Widen window slightly for correlation

        List<String> potentialCauses = new ArrayList<>();
        List<String> suggestions = new ArrayList<>(alert.getSuggestions() != null ? alert.getSuggestions() : List.of());

        // Analyze CPU Usage
        List<Metric> cpuMetrics = metricRepository.findByNameAndTimestampBetween(CPU_USAGE_METRIC_NAME, startTime, endTime);
        OptionalDouble averageCpuUsage = cpuMetrics.stream().mapToDouble(Metric::getValue).average();

        if (averageCpuUsage.isPresent() && averageCpuUsage.getAsDouble() > HIGH_CPU_THRESHOLD) {
            potentialCauses.add("High CPU utilization (" + String.format("%.2f%%", averageCpuUsage.getAsDouble() * 100) + ") observed around the time of the alert.");
            suggestions.add("Investigate processes consuming high CPU. Profile application for CPU hotspots.");
            log.debug("Alert ID {}: High CPU detected: {}", alert.getId(), averageCpuUsage.getAsDouble());
        }

        // Analyze Memory Usage
        List<Metric> memoryMetrics = metricRepository.findByNameAndTimestampBetween(MEMORY_USAGE_METRIC_NAME, startTime, endTime);
        OptionalDouble averageMemoryUsage = memoryMetrics.stream().mapToDouble(Metric::getValue).average();

        if (averageMemoryUsage.isPresent() && averageMemoryUsage.getAsDouble() > HIGH_MEMORY_THRESHOLD_BYTES) {
            potentialCauses.add("High JVM memory usage (" + String.format("%.2f MB", averageMemoryUsage.getAsDouble() / (1024 * 1024)) + ") observed.");
            suggestions.add("Check for memory leaks. Analyze heap dump. Consider increasing JVM heap size if appropriate.");
            log.debug("Alert ID {}: High Memory detected: {}", alert.getId(), averageMemoryUsage.getAsDouble());
        }

        if (potentialCauses.isEmpty()) {
            potentialCauses.add("No obvious resource contention (CPU/Memory) detected as a direct cause based on available metrics. Further investigation of application logs and specific error messages is recommended.");
        }

        // Update alert
        // Concatenate with existing root cause if any, or set new one.
        String existingRootCause = alert.getRootCause();
        StringBuilder newRootCause = new StringBuilder(existingRootCause != null ? existingRootCause : "Initial analysis indicated " + alert.getType() + ".");
        newRootCause.append(" Further checks suggest: ");
        newRootCause.append(String.join(" ", potentialCauses));
        alert.setRootCause(newRootCause.toString());

        alert.setSuggestions(suggestions); // Replace or append suggestions as needed

        log.info("Updated root cause and suggestions for alert ID: {}", alert.getId());
        // Note: The alert object is modified. It needs to be saved by the calling service if changes are to be persisted.
        // The AnomalyDetectionService already saves the alert once. If this service is called after that,
        // the alert needs to be re-saved.
    }
}
