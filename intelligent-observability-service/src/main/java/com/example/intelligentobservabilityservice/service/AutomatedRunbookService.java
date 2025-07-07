package com.example.intelligentobservabilityservice.service;

import com.example.intelligentobservabilityservice.domain.Alert;

import java.util.List;

/**
 * Service for suggesting and managing automated runbooks for recurring patterns.
 * Placeholder for future implementation.
 */
public interface AutomatedRunbookService {

    /**
     * Suggests potential automated runbooks based on an alert.
     * This would typically look up patterns in the KnowledgeBase.
     * @param alert The alert for which to suggest runbooks.
     * @return A list of suggested runbook identifiers or DTOs.
     */
    List<String> suggestRunbooks(Alert alert);

    /**
     * Retrieves details of a specific runbook.
     * @param runbookId The identifier of the runbook.
     * @return A map or custom DTO representing the runbook details (e.g., steps, script links).
     */
    Map<String, Object> getRunbookDetails(String runbookId);

    // Other potential methods:
    // - Execute runbook (with appropriate safeguards)
    // - Track runbook execution history and success rates
}
