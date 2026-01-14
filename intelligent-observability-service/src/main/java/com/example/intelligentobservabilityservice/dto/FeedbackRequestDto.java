package com.example.intelligentobservabilityservice.dto;

import lombok.Data;

@Data
public class FeedbackRequestDto {
    private Long alertId;
    private String userId; // Can be a username or some identifier
    private String feedbackType; // e.g., "THUMBS_UP", "THUMBS_DOWN", "HELPFUL", "INACCURATE"
    private String comments;
}
