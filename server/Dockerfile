# ==========================================
# STAGE 1: The Builder (The Factory)
# ==========================================
# Using your exact JDK 21 requirement
FROM gradle:8-jdk21 AS builder

# Set the working directory for the build
WORKDIR /home/gradle/src

# Copy all your project files into the factory securely
COPY --chown=gradle:gradle . .

# Compile the fat JAR exactly as you did before
RUN gradle clean buildFatJar --no-daemon

# ==========================================
# STAGE 2: The Runtime (The Delivery Car)
# ==========================================
# Using the lightweight JRE 21 runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# We pull the JAR out of your specific 'server' module folder!
# Note: Ktor's buildFatJar creates a file ending in '-all.jar', so we target that explicitly.
COPY --from=builder /home/gradle/src/server/build/libs/*-all.jar ./ktor-app.jar

EXPOSE 8080

# Start the server
CMD ["java", "-jar", "ktor-app.jar"]