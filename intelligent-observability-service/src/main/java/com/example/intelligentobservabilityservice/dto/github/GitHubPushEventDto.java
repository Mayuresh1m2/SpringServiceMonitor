package com.example.intelligentobservabilityservice.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPushEventDto {
    private String ref;
    private List<GitHubCommitDto> commits;
    private GitHubRepositoryDto repository;
}
