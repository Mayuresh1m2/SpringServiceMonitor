package com.example.intelligentobservabilityservice.controller;

import com.example.intelligentobservabilityservice.domain.Alert;
import com.example.intelligentobservabilityservice.domain.Feedback;
import com.example.intelligentobservabilityservice.dto.FeedbackRequestDto;
import com.example.intelligentobservabilityservice.repository.AlertRepository;
import com.example.intelligentobservabilityservice.repository.FeedbackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // To use application-test.properties
@Transactional // Rollback transactions after each test
class AlertsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Alert savedAlert;

    @BeforeEach
    void setUp() {
        feedbackRepository.deleteAll();
        alertRepository.deleteAll(); // Clean slate before each test

        Alert alert = new Alert();
        alert.setType("TEST_ALERT");
        alert.setSeverity("HIGH");
        alert.setTimestamp(LocalDateTime.now());
        alert.setMetricsSnapshot(Map.of("cpu", 0.9, "memory", 0.8));
        alert.setRootCause("Test root cause");
        alert.setSuggestions(Collections.singletonList("Test suggestion"));
        savedAlert = alertRepository.save(alert);
    }

    @Test
    void testGetAlertById_found() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/{id}", savedAlert.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(savedAlert.getId().intValue())))
                .andExpect(jsonPath("$.type", is("TEST_ALERT")))
                .andExpect(jsonPath("$.severity", is("HIGH")));
    }

    @Test
    void testGetAlertById_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/{id}", -1L)) // Non-existent ID
                .andExpect(status().isNotFound());
    }

    @Test
    void testSubmitFeedback_success() throws Exception {
        FeedbackRequestDto feedbackRequest = new FeedbackRequestDto();
        feedbackRequest.setAlertId(savedAlert.getId());
        feedbackRequest.setUserId("testUser");
        feedbackRequest.setFeedbackType("THUMBS_UP");
        feedbackRequest.setComments("Very helpful!");

        mockMvc.perform(post("/api/v1/alerts/{id}/feedback", savedAlert.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is("testUser")))
                .andExpect(jsonPath("$.feedbackType", is("THUMBS_UP")))
                .andExpect(jsonPath("$.comments", is("Very helpful!")))
                .andExpect(jsonPath("$.alert.id", is(savedAlert.getId().intValue())));

        List<Feedback> feedbackList = feedbackRepository.findByAlertId(savedAlert.getId());
        assertEquals(1, feedbackList.size());
        assertEquals("testUser", feedbackList.get(0).getUserId());
    }

    @Test
    void testSubmitFeedback_alertNotFound() throws Exception {
        FeedbackRequestDto feedbackRequest = new FeedbackRequestDto();
        feedbackRequest.setAlertId(-1L); // Non-existent alert ID
        feedbackRequest.setUserId("testUser");
        feedbackRequest.setFeedbackType("THUMBS_UP");
        feedbackRequest.setComments("Doesn't matter");

        mockMvc.perform(post("/api/v1/alerts/{id}/feedback", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isNotFound()) // Because alertId in DTO is validated by service
                .andExpect(content().string("Alert not found with ID: -1"));
    }

    @Test
    void testSubmitFeedback_pathIdMismatchBodyId() throws Exception {
        FeedbackRequestDto feedbackRequest = new FeedbackRequestDto();
        feedbackRequest.setAlertId(savedAlert.getId() + 1); // Different ID in body
        feedbackRequest.setUserId("testUser");
        feedbackRequest.setFeedbackType("THUMBS_UP");
        feedbackRequest.setComments("ID Mismatch");

        mockMvc.perform(post("/api/v1/alerts/{id}/feedback", savedAlert.getId()) // Path ID is savedAlert.getId()
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Alert ID in path (" + savedAlert.getId() + ") does not match Alert ID in request body (" + (savedAlert.getId()+1) + ")."));
    }


    @Test
    void testGetFeedbackForAlert_found() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setAlert(savedAlert);
        feedback.setUserId("user1");
        feedback.setFeedbackType("HELPFUL");
        feedback.setComments("Good one");
        feedbackRepository.save(feedback);

        Feedback feedback2 = new Feedback();
        feedback2.setAlert(savedAlert);
        feedback2.setUserId("user2");
        feedback2.setFeedbackType("NEEDS_IMPROVEMENT");
        feedback2.setComments("Could be better");
        feedbackRepository.save(feedback2);

        mockMvc.perform(get("/api/v1/alerts/{id}/feedback", savedAlert.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is("user1")))
                .andExpect(jsonPath("$[1].userId", is("user2")));
    }

    @Test
    void testGetFeedbackForAlert_noneFound() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/{id}/feedback", savedAlert.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetFeedbackForAlert_alertNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/{id}/feedback", -1L)) // Non-existent alert ID
                .andExpect(status().isNotFound());
    }
}
