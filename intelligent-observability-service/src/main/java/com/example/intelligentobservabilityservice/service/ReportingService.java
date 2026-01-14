package com.example.intelligentobservabilityservice.service;

import com.example.intelligentobservabilityservice.domain.Alert; // Assuming reports might be alert-centric

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for generating reports and historical analytics.
 * Placeholder for future implementation.
 */
public interface ReportingService {

    /**
     * Generates a summary report of alerts within a given time period.
     * @param startTime Start of the reporting period.
     * @param endTime End of the reporting period.
     * @return A map or a custom DTO representing the report.
     */
    Map<String, Object> generateAlertSummaryReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Analyzes historical data to find what actions were effective for similar issues.
     * @param currentAlert The current alert for which to find historical insights.
     * @return A list of strings or custom DTOs describing effective past actions.
     */
    List<String> getHistoricalEffectiveness(Alert currentAlert);

    // Other potential reporting methods:
    // - Trend analysis of specific metrics
    // - MTTR (Mean Time To Resolution) analysis
    // - Frequency of different alert types
}
