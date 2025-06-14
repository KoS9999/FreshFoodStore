FROM openjdk:21-jdk-slim
WORKDIR /app

COPY target/foodstore-0.0.1-SNAPSHOT.jar app.jar
COPY uploads /app/uploads
RUN mkdir -p /app/uploads && chmod -R 755 /app/uploads

ENTRYPOINT ["java", "-jar", "app.jar"]
