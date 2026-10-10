# Build stage with Maven and OpenJDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and package jar
COPY src ./src
RUN mvn clean package -DskipTests

# Lightweight JRE 21 runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as unprivileged user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built artifact
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Constrain heap to avoid OOM killer on Render's 512MB container
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xmx384m", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
