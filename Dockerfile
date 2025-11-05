# Use Java 21 image (Temurin is great)
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Maven files first for dependency caching
COPY pom.xml .
RUN apt-get update && apt-get install -y maven && mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the app
RUN mvn clean package -DskipTests

# Run the built JAR
CMD ["java", "-jar", "target/pdf-service-1.0.0.jar"]
