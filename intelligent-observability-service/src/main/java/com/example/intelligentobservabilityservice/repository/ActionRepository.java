package com.example.intelligentobservabilityservice.repository;

import com.example.intelligentobservabilityservice.domain.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
}
