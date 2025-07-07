package com.example.intelligentobservabilityservice.controller;

import com.example.intelligentobservabilityservice.domain.Metric;
import com.example.intelligentobservabilityservice.service.MetricsIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics Ingestion", description = "APIs for ingesting metrics into the service.")
public class MetricsController {

    private final MetricsIngestionService metricsIngestionService;

    @PostMapping("/ingest")
    @Operation(summary = "Ingest Multiple Metrics", description = "Ingests a list of metrics into the system.")
    public ResponseEntity<String> ingestMetrics(@RequestBody List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return ResponseEntity.badRequest().body("Metrics list cannot be null or empty.");
        }
        try {
            metricsIngestionService.ingestMetrics(metrics);
            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully ingested " + metrics.size() + " metrics.");
        } catch (Exception e) {
            // Log the exception details here
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error ingesting metrics: " + e.getMessage());
        }
    }

    @PostMapping("/ingest-single")
    @Operation(summary = "Ingest Single Metric", description = "Ingests a single metric into the system.")
    public ResponseEntity<String> ingestSingleMetric(@RequestBody Metric metric) {
        if (metric == null) {
            return ResponseEntity.badRequest().body("Metric cannot be null.");
        }
        try {
            metricsIngestionService.ingestMetric(metric);
            return ResponseEntity.status(HttpStatus.CREATED).body("Successfully ingested metric: " + metric.getName());
        } catch (Exception e) {
            // Log the exception details here
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error ingesting metric: " + e.getMessage());
        }
    }
}
