FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ----------------------

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=build /build/target/foodstore-0.0.1-SNAPSHOT.jar app.jar
COPY uploads /app/uploads
RUN mkdir -p /app/uploads && chmod -R 755 /app/uploads

ENTRYPOINT ["java", "-jar", "app.jar"]
