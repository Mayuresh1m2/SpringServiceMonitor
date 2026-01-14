package com.example.intelligentobservabilityservice.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCommitAuthorDto {
    private String name;
    private String email;
    private String username; // GitHub username
}
