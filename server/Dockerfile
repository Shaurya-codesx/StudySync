# ==========================================
# STAGE 1: The Builder (The Factory)
# ==========================================
FROM gradle:8-jdk21 AS builder

# Set the working directory
WORKDIR /home/gradle/src

# Copy the server files (Railway will pass ONLY the server folder)
COPY --chown=gradle:gradle . .

# Run the build
RUN gradle clean buildFatJar --no-daemon

# ==========================================
# STAGE 2: The Runtime (The Delivery Car)
# ==========================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# The path is now shorter because the 'server' root is already established
COPY --from=builder /home/gradle/src/build/libs/*-all.jar ./ktor-app.jar

EXPOSE 8080

# Start the server
CMD ["java", "-jar", "ktor-app.jar"]