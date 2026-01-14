package com.example.intelligentobservabilityservice.repository;

import com.example.intelligentobservabilityservice.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByAlertId(Long alertId);
}
