package com.example.intelligentobservabilityservice.controller;

import com.example.intelligentobservabilityservice.dto.github.GitHubCommitDto;
import com.example.intelligentobservabilityservice.dto.github.GitHubPushEventDto;
import com.example.intelligentobservabilityservice.service.ActionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "APIs for receiving webhook events from external systems (e.g., GitHub).")
public class GitHubWebhookController {

    private final ActionService actionService;
    private final ObjectMapper objectMapper; // For parsing if needed, or Spring handles it

    // Regex to find alert IDs like "ALERT-123" or "alert-123"
    // It looks for keywords like Fixes, Resolves, Closes, Addresses
    // Then an optional #, then "ALERT-" (case-insensitive), then digits.
    private static final Pattern ALERT_ID_PATTERN = Pattern.compile(
            "(?i)(?:Fixes|Resolves|Closes|Addresses)\\s+#?ALERT-(\\d+)"
    );

    @PostMapping("/github")
    @Operation(summary = "Handle GitHub Webhook", description = "Receives webhook events from GitHub. Currently processes 'push' events to link commits to alerts.")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestHeader("X-GitHub-Event") String githubEvent,
            @RequestBody String payloadBody) { // Receive as String to log raw payload first

        log.info("Received GitHub webhook. Event: {}", githubEvent);
        log.debug("Payload: {}", payloadBody);

        if (!"push".equalsIgnoreCase(githubEvent)) {
            log.info("Ignoring GitHub event '{}' as it is not a 'push' event.", githubEvent);
            return ResponseEntity.ok("Event not processed as it's not a 'push' event.");
        }

        try {
            GitHubPushEventDto pushEvent = objectMapper.readValue(payloadBody, GitHubPushEventDto.class);

            if (pushEvent.getCommits() == null || pushEvent.getCommits().isEmpty()) {
                log.info("No commits found in the push event.");
                return ResponseEntity.ok("No commits to process.");
            }

            int actionsRecorded = 0;
            for (GitHubCommitDto commit : pushEvent.getCommits()) {
                if (commit.getMessage() == null || commit.getMessage().isEmpty()) {
                    log.debug("Commit {} has no message, skipping.", commit.getId());
                    continue;
                }

                Matcher matcher = ALERT_ID_PATTERN.matcher(commit.getMessage());
                while (matcher.find()) {
                    String alertIdStr = matcher.group(1);
                    try {
                        Long alertId = Long.parseLong(alertIdStr);
                        log.info("Found reference to Alert ID {} in commit {} message: '{}'", alertId, commit.getId(), commit.getMessage());
                        actionService.recordCommitAction(alertId, commit, commit.getMessage());
                        actionsRecorded++;
                    } catch (NumberFormatException e) {
                        log.error("Could not parse alert ID '{}' from commit message.", alertIdStr, e);
                    } catch (Exception e) {
                        log.error("Error recording action for alert ID {} from commit {}: {}", alertIdStr, commit.getId(), e.getMessage(), e);
                    }
                }
            }
            return ResponseEntity.ok("Webhook processed. Recorded " + actionsRecorded + " actions from commit messages.");

        } catch (Exception e) {
            log.error("Error processing GitHub webhook payload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing payload.");
        }
    }
}
