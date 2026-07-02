# Step 1: Use Java 21 as base image
# this gives us Java environment to run our app
FROM eclipse-temurin:21-jre-alpine

# Step 2: Set working directory inside container
# all commands will run from this folder
WORKDIR /app

# Step 3: Copy JAR file from target folder to container
# target/quiz-game-app-0.0.1-SNAPSHOT.jar → /app/app.jar
COPY target/quiz-game-app-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Tell Docker which port our app runs on
# Spring Boot runs on 9090
EXPOSE 8080

# Step 5: Command to run when container starts
# java -jar app.jar starts our Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]