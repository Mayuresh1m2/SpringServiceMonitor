package com.example.intelligentobservabilityservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "knowledge_base_entries")
@Data
@NoArgsConstructor
public class KnowledgeBaseEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patternName; // e.g., "DB_LATENCY_CAUSES_5XX"
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> conditions; // e.g., {"metric": "db.query.latency", "threshold": "100ms"}

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> correlations; // e.g., {"correlatedMetric": "http.server.requests.5xx.count"}

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> suggestedActions; // e.g., {"action1": "Increase DB pool size"}

    private double effectivenessScore; // 0.0 to 1.0, learned over time
    private int usageCount; // How many times this entry was used

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        effectivenessScore = 0.5; // Default score
        usageCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
