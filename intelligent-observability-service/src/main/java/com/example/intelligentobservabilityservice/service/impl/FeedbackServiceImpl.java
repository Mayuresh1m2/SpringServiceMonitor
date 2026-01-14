package com.example.intelligentobservabilityservice.service.impl;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Feedback;
import com.example.intelligentobservabilityservice.dto.FeedbackRequestDto;
import com.example.intelligentobservabilityservice.repository.AlertRepository;
import com.example.intelligentobservabilityservice.repository.FeedbackRepository;
import com.example.intelligentobservabilityservice.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AlertRepository alertRepository; // To link feedback to an alert

    @Override
    @Transactional
    public Feedback saveFeedback(FeedbackRequestDto feedbackRequestDto) {
        Optional<Alert> alertOptional = alertRepository.findById(feedbackRequestDto.getAlertId());
        if (alertOptional.isEmpty()) {
            log.error("Alert not found with ID: {}", feedbackRequestDto.getAlertId());
            throw new IllegalArgumentException("Alert not found with ID: " + feedbackRequestDto.getAlertId());
            // Or handle with a custom exception leading to a 404
        }

        Feedback feedback = new Feedback();
        feedback.setAlert(alertOptional.get());
        feedback.setUserId(feedbackRequestDto.getUserId());
        feedback.setFeedbackType(feedbackRequestDto.getFeedbackType());
        feedback.setComments(feedbackRequestDto.getComments());

        Feedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Saved feedback ID {} for Alert ID {}", savedFeedback.getId(), feedbackRequestDto.getAlertId());

        // Future: Trigger learning module based on this feedback
        // learnFromFeedback(savedFeedback);

        return savedFeedback;
    }

    @Override
    public Optional<Feedback> getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId);
    }

    @Override
    public List<Feedback> getFeedbackForAlert(Long alertId) {
        return feedbackRepository.findByAlertId(alertId);
    }

    // private void learnFromFeedback(Feedback feedback) {
    //     // Placeholder for learning logic
    //     log.info("Processing feedback for learning: Feedback ID {}", feedback.getId());
    //     // This would interact with the KnowledgeBase or ML models
    // }
}
