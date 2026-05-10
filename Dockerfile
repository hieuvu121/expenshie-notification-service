FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY common/pom.xml ./common/pom.xml
COPY common/src ./common/src
RUN mvn -f common/pom.xml install -DskipTests -B

COPY notification-service/pom.xml ./notification-service/pom.xml
RUN mvn -f notification-service/pom.xml dependency:go-offline -B

COPY notification-service/src ./notification-service/src
RUN mvn -f notification-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/notification-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
