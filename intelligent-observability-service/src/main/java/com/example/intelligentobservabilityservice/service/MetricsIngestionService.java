package com.example.intelligentobservabilityservice.service;

import com.example.intelligentobservabilityservice.domain.Metric;
import java.util.List;

public interface MetricsIngestionService {
    void ingestMetrics(List<Metric> metrics);
    void ingestMetric(Metric metric); // For ingesting a single metric
}
