FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S mathinclui && adduser -S mathinclui -G mathinclui
COPY --from=build /app/target/mathinclui-backend.jar app.jar
USER mathinclui
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
