
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/bank-transfer-service.jar bank-transfer-service.jar

EXPOSE 8290

ENTRYPOINT ["java", "-jar", "bank-transfer-service.jar"]