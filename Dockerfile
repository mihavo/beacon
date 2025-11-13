ARG SERVICE_NAME
ENV SERVER_PORT=8080
ENV JAVA_OPTS=""


FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml ./
COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}

RUN mvn -f location-service/pom.xml dependency:go-offline -B
COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src
RUN mvn -f ${SERVICE_NAME}/pom.xml clean package -DskipTests


FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=build /app/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh" ,"-c", "java $JAVA_OPTS -jar app.jar"]