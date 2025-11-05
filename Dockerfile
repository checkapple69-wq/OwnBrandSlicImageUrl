# Use a reliable Java 17 image
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven and build
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# Run the jar file (replace name if needed)
CMD ["java", "-jar", "target/OwnBrandSlicImageUrl-0.0.1-SNAPSHOT.jar"]
