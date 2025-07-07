package com.example.intelligentobservabilityservice.service;

import com.example.intelligentobservabilityservice.domain.Alert;

public interface RootCauseSuggestionService {

    /**
     * Analyzes an alert and attempts to suggest potential root causes and actions.
     * This method may modify the provided Alert object by adding root cause information
     * and suggestions.
     *
     * @param alert The alert to analyze.
     */
    void suggestRootCause(Alert alert);
}
