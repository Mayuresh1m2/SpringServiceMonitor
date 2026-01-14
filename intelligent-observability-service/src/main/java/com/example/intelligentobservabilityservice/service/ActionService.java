package com.example.intelligentobservabilityservice.service;

import com.example.intelligentobservabilityservice.domain.Action;
import com.example.intelligentobservabilityservice.dto.GitHubCommitDto; // Will create this DTO

public interface ActionService {
    Action recordCommitAction(Long alertId, GitHubCommitDto commitDto, String commitMessage);
}
