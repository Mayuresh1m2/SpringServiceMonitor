package com.example.intelligentobservabilityservice.repository;

import com.example.intelligentobservabilityservice.domain.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    List<Metric> findByNameAndTimestampBetween(String name, LocalDateTime start, LocalDateTime end);

    List<Metric> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

}
