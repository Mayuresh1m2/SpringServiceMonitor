package com.example.intelligentobservabilityservice.cli;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.service.AnomalyDetectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class DiagnosticsCliCommands {

    private final AnomalyDetectionService anomalyDetectionService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @ShellMethod(key = "trigger-diagnostics", value = "Manually triggers the anomaly detection process.")
    public String triggerDiagnostics(
            @ShellOption(defaultValue = "15", help = "Time window in minutes to fetch metrics for.") int timeWindowInMinutes) {
        if (timeWindowInMinutes <= 0) {
            return "Error: timeWindowInMinutes must be a positive integer.";
        }

        try {
            List<Alert> alerts = anomalyDetectionService.runDetectionStrategies(timeWindowInMinutes);
            if (alerts.isEmpty()) {
                return "Diagnostics run completed. No new anomalies detected.";
            }
            // Pretty print the alerts list as JSON
            return "Diagnostics run completed. Detected anomalies:\n" +
                   objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(alerts);
        } catch (JsonProcessingException e) {
            return "Error formatting alerts to JSON: " + e.getMessage();
        } catch (Exception e) {
            // Log the full exception server-side
            return "An error occurred while triggering diagnostics: " + e.getMessage();
        }
    }
}
