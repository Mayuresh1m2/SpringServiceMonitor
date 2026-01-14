package com.example.intelligentobservabilityservice.repository;

import com.example.intelligentobservabilityservice.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
}
