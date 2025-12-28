FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY build/libs/proxy-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8000

EXPOSE ${PORT}

CMD ["java", "-Dserver.port=${PORT}", "-jar", "/app/app.jar"]