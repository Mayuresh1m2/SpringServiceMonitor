# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
#RUN ./mvnw dependency:go-offline -B
# Using a simpler dependency download, go-offline can be more robust but slower if not cached
RUN ./mvnw dependency:resolve

# Copy the rest of the application source code
COPY src src

# Build the application executable JAR
RUN ./mvnw package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the executable JAR from the builder stage
COPY --from=builder /app/target/intelligent-observability-service-*.jar app.jar

# Expose the application port (though docker-compose also defines this)
EXPOSE 8080

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: Add a non-root user for security
# USER appuser
# RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
# USER appuser
