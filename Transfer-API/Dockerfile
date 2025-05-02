#FROM eclipse-temurin:17-jre-alpine
#
#
## Define user properties
#ARG USERNAME=appuser
#
#WORKDIR /app
#
#COPY target/Transfer-API.jar /app/Transfer-API.jar
#
## Create a non-root user
#RUN addgroup -S appgroup && adduser -S $USERNAME -G appgroup
#
#
## Set the User to run the service
#USER $USERNAME
#
## Set the port the service is to be exposed on.
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "Transfer-API.jar"]


# Start with a base image that includes Java
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file into the image
COPY target/bank-transfer-service.jar bank-transfer-service.jar

# Expose port
EXPOSE 8290

# Command to run the app
ENTRYPOINT ["java", "-jar", "bank-transfer-service.jar"]