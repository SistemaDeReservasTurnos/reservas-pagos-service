# -----------------------------------------------------------------
# STAGE 1: BUILD
# Uses a robust image with all necessary tools for compilation (Maven + JDK).
# -----------------------------------------------------------------
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# Working directory inside the container
WORKDIR /app

# Copy the Maven project file
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src /app/src

# Build the project, skipping tests to speed up the process
RUN mvn clean package -DskipTests

# -----------------------------------------------------------------
# STAGE 2: RUNTIME
# Uses a minimal base image with only the JRE (Java Runtime Environment).
# This creates a small and secure final image.
# -----------------------------------------------------------------
# Using a Slim/Alpine JRE for a smaller footprint
FROM eclipse-temurin:17-jre-alpine

# Define the application's exposed port
EXPOSE 8084

# Working directory
WORKDIR /app

# Copy ONLY the compiled JAR file from the 'builder' stage
COPY --from=builder /app/target/reservas-pagos-service.jar reservas-pagos-service.jar

# Configure the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "reservas-pagos-service.jar"]