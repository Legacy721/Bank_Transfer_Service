FROM eclipse-temurin:17-jre-alpine


# Define user properties
ARG USERNAME=appuser

WORKDIR /app

COPY target/Transfer-API.jar /app/Transfer-API.jar

# Create a non-root user
RUN addgroup -S appgroup && adduser -S $USERNAME -G appgroup


# Set the User to run the service
USER $USERNAME

# Set the port the service is to be exposed on.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "Transfer-API.jar"]