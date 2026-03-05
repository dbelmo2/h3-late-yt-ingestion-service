# Stage 1: Build the application
# Using the official Maven image with Java 25 pre-installed
FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /build

# 1. Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final production image
FROM eclipse-temurin:25-jre-jammy
# Security: Create a non-root user
RUN useradd -m h3user
USER h3user
WORKDIR /app

# Copy the built JAR from the first stage
COPY --from=build /build/target/*.jar app.jar

# JVM Flags for Railway's $5 plan (300MB limit)
ENTRYPOINT ["java", "-Xmx300M", "-Xms300M", "-jar", "app.jar"]