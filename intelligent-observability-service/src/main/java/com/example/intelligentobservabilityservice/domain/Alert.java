package com.example.intelligentobservabilityservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String type; // e.g., "HIGH_5XX", "HIGH_LATENCY"
    private String severity; // e.g., "CRITICAL", "WARNING"

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metricsSnapshot; // Snapshot of relevant metrics at the time of alert

    @Lob
    private String rootCause; // Suggested root cause text

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alert_suggestions", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "suggestion")
    private List<String> suggestions; // List of suggested actions

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
