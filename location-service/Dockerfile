FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml ./
COPY location-service/pom.xml location-service/
RUN mvn -f location-service/pom.xml dependency:go-offline -B
COPY location-service/src location-service/src
RUN mvn -f location-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=build /app/location-service/target/*.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 8083
ENTRYPOINT ["sh" ,"-c", "java $JAVA_OPTS -jar app.jar"]