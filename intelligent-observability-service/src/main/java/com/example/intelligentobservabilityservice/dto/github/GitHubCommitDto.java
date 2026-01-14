package com.example.intelligentobservabilityservice.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCommitDto { // Renamed from the one in ActionService for clarity
    private String id; // This is the commit hash
    private String message;
    private GitHubCommitAuthorDto author;
    private String url; // URL to the commit
}
