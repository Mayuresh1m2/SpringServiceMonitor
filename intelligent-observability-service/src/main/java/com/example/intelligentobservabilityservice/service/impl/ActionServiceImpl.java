package com.example.intelligentobservabilityservice.service.impl;

import com.example.intelligentobservabilityservice.domain.Action;
import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.dto.github.GitHubCommitDto;
import com.example.intelligentobservabilityservice.repository.ActionRepository;
import com.example.intelligentobservabilityservice.repository.AlertRepository;
import com.example.intelligentobservabilityservice.service.ActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionServiceImpl implements ActionService {

    private final ActionRepository actionRepository;
    private final AlertRepository alertRepository;

    @Override
    @Transactional
    public Action recordCommitAction(Long alertId, GitHubCommitDto commitDto, String commitMessage) {
        Optional<Alert> alertOptional = alertRepository.findById(alertId);
        if (alertOptional.isEmpty()) {
            log.warn("Attempted to record action for non-existent Alert ID: {}", alertId);
            return null; // Or throw an exception
        }

        Action action = new Action();
        action.setAlert(alertOptional.get());
        action.setCommitHash(commitDto.getId());

        String description = "Commit " + commitDto.getId().substring(0, 7) + " by " +
                             (commitDto.getAuthor() != null ? commitDto.getAuthor().getName() : "unknown author") +
                             ": \"" + commitMessage + "\"";
        action.setDescription(description);
        action.setResolutionStatus("INVESTIGATING"); // Default status, could be updated later

        Action savedAction = actionRepository.save(action);
        log.info("Recorded action ID {} for Alert ID {} based on commit {}", savedAction.getId(), alertId, commitDto.getId());
        return savedAction;
    }
}
