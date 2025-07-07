package com.example.intelligentobservabilityservice.service.impl;

import com.example.intelligentobservabilityservice.domain.Metric;
import com.example.intelligentobservabilityservice.repository.MetricRepository;
import com.example.intelligentobservabilityservice.service.MetricsIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsIngestionServiceImpl implements MetricsIngestionService {

    private final MetricRepository metricRepository;

    @Override
    @Transactional
    public void ingestMetrics(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            log.warn("Attempted to ingest null or empty metrics list.");
            return;
        }
        // In a high-throughput scenario, consider batching saves if not handled by the driver/JPA provider.
        // For now, saveAll will suffice.
        metricRepository.saveAll(metrics);
        log.info("Successfully ingested {} metrics.", metrics.size());
    }

    @Override
    @Transactional
    public void ingestMetric(Metric metric) {
        if (metric == null) {
            log.warn("Attempted to ingest null metric.");
            return;
        }
        metricRepository.save(metric);
        log.info("Successfully ingested metric: {}", metric.getName());
    }
}
