# Use the official OpenJDK base image with Java 17
FROM openjdk:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from the local filesystem to the container
COPY target/cryptopricereader-0.0.1-SNAPSHOT.jar /app/app.jar

# Copy the entire Java_Assignment_Exercise directory
COPY Java_Assignment_Exercise /app/Java_Assignment_Exercise

# Expose the port that the application will run on
EXPOSE 8080

# Define the command to run the application when the container starts
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=container"]

