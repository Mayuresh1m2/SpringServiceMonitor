# Intelligent Observability Insights & Recommendation Service

This project is a Spring Boot-compatible diagnostic service designed to continuously or manually analyze metrics from a Spring Boot application. It aims to automatically identify probable root causes for anomalies and generate actionable recommendations, learning over time from user feedback and commit hooks.

## Features

*   **Metrics Ingestion**: Collects and stores metrics (e.g., via REST API).
*   **Anomaly Detection**: Rule-based detection of anomalies (e.g., high 5xx errors).
*   **Root Cause Suggestion**: Initial suggestions based on correlated metrics (e.g., CPU, memory for 5xx errors).
*   **Manual Trigger**: REST API to manually trigger diagnostic analysis.
*   **Feedback Mechanism**: API to submit user feedback on alert suggestions.
*   **GitHub Integration**: Webhook listener to link commits to alerts based on commit messages.
*   **Database**: Uses PostgreSQL, with schema managed by Flyway.
*   **API Documentation**: OpenAPI v3 documentation available via Swagger UI.

## Prerequisites

*   Java 17 or higher
*   Maven 3.6.x or higher
*   PostgreSQL server (running and accessible)

## Setup

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd intelligent-observability-service
    ```

2.  **Configure Database:**
    *   Ensure you have a PostgreSQL database created.
    *   Update the database connection details in `src/main/resources/application.properties`:
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/observability_db
        spring.datasource.username=your_db_user
        spring.datasource.password=your_db_password
        ```
    *   Flyway will automatically create and manage the schema upon application startup.

3.  **Build the project:**
    ```bash
    mvn clean install
    ```

4.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```
    Alternatively, you can run the packaged JAR:
    ```bash
    java -jar target/intelligent-observability-service-0.0.1-SNAPSHOT.jar
    ```

## Running with Docker Compose

Alternatively, you can run the application and a PostgreSQL database using Docker Compose:

1.  **Ensure Docker and Docker Compose are installed.**

2.  **Build and run the services:**
    From the project root directory (`intelligent-observability-service`):
    ```bash
    docker-compose up --build
    ```
    This command will:
    *   Build the Docker image for the `observability-service` using the provided `Dockerfile`.
    *   Pull the `postgres:15` image if not already present.
    *   Start both the application and database containers.
    *   The application will be available on `http://localhost:8080`.
    *   PostgreSQL will be available on `localhost:5432` (primarily for direct inspection if needed, the app connects internally via the Docker network).

3.  **To stop the services:**
    Press `Ctrl+C` in the terminal where `docker-compose up` is running, then:
    ```bash
    docker-compose down
    ```
    To remove volumes (and thus delete PostgreSQL data):
    ```bash
    docker-compose down -v
    ```

## API Documentation (Swagger UI)

Once the application is running (either locally or via Docker Compose), the OpenAPI documentation (Swagger UI) can be accessed at:

*   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

The OpenAPI specification JSON can be found at:

*   [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Key API Endpoints

*   **Metrics Ingestion**:
    *   `POST /api/v1/metrics/ingest`: Ingest a list of metrics.
    *   `POST /api/v1/metrics/ingest-single`: Ingest a single metric.
*   **Diagnostics**:
    *   `POST /api/v1/diagnostics/trigger`: Manually trigger anomaly detection (optional `timeWindowInMinutes` parameter).
*   **Alerts & Feedback**:
    *   `GET /api/v1/alerts/{id}`: Get details of a specific alert.
    *   `POST /api/v1/alerts/{id}/feedback`: Submit feedback for an alert.
    *   `GET /api/v1/alerts/{id}/feedback`: Get all feedback for an alert.
*   **Webhooks**:
    *   `POST /api/v1/webhooks/github`: Endpoint for GitHub push event webhooks.

## Modules Overview

*   **`com.example.intelligentobservabilityservice.controller`**: REST API controllers.
*   **`com.example.intelligentobservabilityservice.domain`**: JPA entities.
*   **`com.example.intelligentobservabilityservice.dto`**: Data Transfer Objects for API requests/responses and external systems (e.g., GitHub).
*   **`com.example.intelligentobservabilityservice.repository`**: Spring Data JPA repositories.
*   **`com.example.intelligentobservabilityservice.service`**: Service interfaces.
*   **`com.example.intelligentobservabilityservice.service.impl`**: Service implementations.
*   **`com.example.intelligentobservabilityservice.service.anomaly`**: Anomaly detection strategies.
*   **`src/main/resources/db/migration`**: Flyway SQL migration scripts.

## Future Enhancements (as per original prompt)

*   Pluggable exporters for metrics (Prometheus, Datadog, etc.).
*   ML-based anomaly detection.
*   More sophisticated root cause correlation.
*   Integration with alerting tools (Prometheus Alertmanager, PagerDuty).
*   Learning from feedback to update the knowledge base.
*   Analysis of metrics pre- and post-change for validation.
*   Reporting dashboards.
*   Automated runbook suggestions and execution.
*   OAuth2 security for APIs and RBAC for dashboards.

---

This `README.md` provides a good starting point for users to understand, set up, and use the service.Tool output for `create_file_with_block`:
