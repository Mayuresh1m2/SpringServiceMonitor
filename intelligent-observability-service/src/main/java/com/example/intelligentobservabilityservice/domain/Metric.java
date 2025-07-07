package com.example.intelligentobservabilityservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "metrics", indexes = {
        @Index(name = "idx_metric_name_timestamp", columnList = "name, timestamp"),
        @Index(name = "idx_metric_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "http.server.requests", "jvm.memory.used"
    private String namespace; // e.g., "application", "jvm"
    private String serviceName;
    private String serviceVersion;
    private String deploymentId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> tags; // Micrometer tags: status="500", exception="NullPointerException"

    private Double value; // The metric value

    private LocalDateTime timestamp; // Timestamp of the metric data point

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Note: No @PreUpdate as metrics are typically immutable once recorded.
}
