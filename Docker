FROM openjdk:17-jdk-slim
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests
CMD ["java", "-jar", "target/OwnBrandSlicImageUrl-0.0.1-SNAPSHOT.jar"]
