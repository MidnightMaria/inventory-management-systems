# Build stage - using official Maven image with Java 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Build application
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage - using slim JRE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy built jar
COPY --from=build /app/target/spring-boot-boilerplate.jar app.jar

# Set non-root user
RUN useradd -m myuser && chown -R myuser:myuser /app
USER myuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]