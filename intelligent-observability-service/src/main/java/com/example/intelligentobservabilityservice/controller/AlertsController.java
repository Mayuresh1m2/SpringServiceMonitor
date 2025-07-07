package com.example.intelligentobservabilityservice.controller;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Feedback;
import com.example.intelligentobservabilityservice.dto.FeedbackRequestDto;
import com.example.intelligentobservabilityservice.repository.AlertRepository;
import com.example.intelligentobservabilityservice.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts Management", description = "APIs for managing alerts and their associated feedback.")
public class AlertsController {

    private final AlertRepository alertRepository;
    private final FeedbackService feedbackService;

    /**
     * Get details of a specific alert.
     * @param id The ID of the alert.
     * @return The alert details or 404 if not found.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Alert by ID", description = "Retrieves the details of a specific alert by its unique ID.")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        Optional<Alert> alert = alertRepository.findById(id);
        return alert.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Submit feedback for a specific alert.
     * @param id The ID of the alert to which this feedback pertains.
     * @param feedbackRequest The feedback data.
     * @return The created feedback or an error response.
     */
    @PostMapping("/{id}/feedback")
    @Operation(summary = "Submit Feedback for an Alert", description = "Submits feedback (e.g., user rating, comments) for a specific alert.")
    public ResponseEntity<?> submitFeedback(@PathVariable Long id, @RequestBody FeedbackRequestDto feedbackRequest) {
        // Ensure the feedback is for the alert specified in the path
        if (!id.equals(feedbackRequest.getAlertId())) {
            return ResponseEntity.badRequest().body("Alert ID in path (" + id + ") does not match Alert ID in request body (" + feedbackRequest.getAlertId() + ").");
        }

        try {
            Feedback savedFeedback = feedbackService.saveFeedback(feedbackRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeedback);
        } catch (IllegalArgumentException e) { // Catching exception from FeedbackServiceImpl if alert not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Log exception e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting feedback: " + e.getMessage());
        }
    }

    /**
     * Get all feedback associated with a specific alert.
     * @param id The ID of the alert.
     * @return A list of feedback for the alert.
     */
    @GetMapping("/{id}/feedback")
    @Operation(summary = "Get Feedback for an Alert", description = "Retrieves all feedback entries associated with a specific alert ID.")
    public ResponseEntity<List<Feedback>> getFeedbackForAlert(@PathVariable Long id) {
        if (!alertRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<Feedback> feedbackList = feedbackService.getFeedbackForAlert(id);
        return ResponseEntity.ok(feedbackList);
    }
}
