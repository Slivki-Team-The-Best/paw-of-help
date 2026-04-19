# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package \
    && find target -maxdepth 1 -name "*.jar" ! -name "original-*" -exec cp {} /app/application.jar \;

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/application.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
