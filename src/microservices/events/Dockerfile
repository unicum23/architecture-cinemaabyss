FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY build/libs/events-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8082

EXPOSE ${PORT}

CMD ["java", "-Dserver.port=${PORT}", "-jar", "/app/app.jar"]