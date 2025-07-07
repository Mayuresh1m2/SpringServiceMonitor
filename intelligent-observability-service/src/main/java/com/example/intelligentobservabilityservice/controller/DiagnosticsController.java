package com.example.intelligentobservabilityservice.controller;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.service.AnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/v1/diagnostics")
@RequiredArgsConstructor
@Tag(name = "Diagnostics", description = "APIs for triggering diagnostic processes and anomaly detection.")
public class DiagnosticsController {

    private final AnomalyDetectionService anomalyDetectionService;

    /**
     * Manually triggers the anomaly detection process.
     * @param timeWindowInMinutes The time window (in minutes from now) to fetch metrics for. Defaults to 15 minutes.
     * @return A list of alerts generated, if any.
     */
    @PostMapping("/trigger")
    @Operation(summary = "Trigger Diagnostics", description = "Manually triggers the anomaly detection process for a specified recent time window.")
    public ResponseEntity<?> triggerDiagnostics(
            @RequestParam(defaultValue = "15") int timeWindowInMinutes) {
        try {
            if (timeWindowInMinutes <= 0) {
                return ResponseEntity.badRequest().body("timeWindowInMinutes must be positive.");
            }
            List<Alert> alerts = anomalyDetectionService.runDetectionStrategies(timeWindowInMinutes);
            if (alerts.isEmpty()) {
                return ResponseEntity.ok("Diagnostics run completed. No new anomalies detected.");
            }
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            // Log exception e
            return ResponseEntity.status(500).body("Error triggering diagnostics: " + e.getMessage());
        }
    }
}
