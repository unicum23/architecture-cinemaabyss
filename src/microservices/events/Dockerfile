FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew bootJar -x test

FROM eclipse-temurin:17-jdk
VOLUME /tmp
WORKDIR /
COPY --from=builder /app/build/libs/*.jar /events-service.jar
ENTRYPOINT ["java", "-jar", "/events-service.jar"]