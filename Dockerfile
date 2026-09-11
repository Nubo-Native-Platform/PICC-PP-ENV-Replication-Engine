# Production Runtime Container
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

# Create a non-root system user for security compliance
RUN groupadd -r appgroup && useradd -r -g appgroup -u 1001 appuser

# Copy application templates
COPY template/ /app/template/

# Copy the pre-built application JAR
COPY target/envrep-engine-0.0.1-SNAPSHOT.jar app.jar

# Create runtime gitops directory with proper permissions
RUN mkdir -p /app/gitops && chown -R appuser:appgroup /app

USER appuser

# Expose HTTP service port
EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
