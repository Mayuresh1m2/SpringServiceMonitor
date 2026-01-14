package com.example.intelligentobservabilityservice.service;

import com.example.intelligentobservabilityservice.domain.Feedback;
import com.example.intelligentobservabilityservice.dto.FeedbackRequestDto; // Will create this DTO

import java.util.List;
import java.util.Optional;

public interface FeedbackService {
    Feedback saveFeedback(FeedbackRequestDto feedbackRequestDto);
    Optional<Feedback> getFeedbackById(Long feedbackId);
    List<Feedback> getFeedbackForAlert(Long alertId);
}
