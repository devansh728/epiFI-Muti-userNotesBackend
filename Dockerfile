FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime with JDK 21 Alpine
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /build/target/notes-*.jar app.jar
RUN chown appuser:appgroup /app
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENV JAVA_OPTS="-XX:+EnablePreviewFeatures -Dspring.profiles.active=prod"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
