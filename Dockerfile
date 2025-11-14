FROM maven:3.9.11-eclipse-temurin-25 AS build
ARG SERVICE_NAME
WORKDIR /app

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2  mvn -N install -DskipTests

COPY core/ core/
RUN --mount=type=cache,target=/root/.m2  mvn -f core/pom.xml clean install -DskipTests
COPY security-core/ security-core/
RUN --mount=type=cache,target=/root/.m2  mvn -f security-core/pom.xml clean install -DskipTests

COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}/pom.xml
RUN --mount=type=cache,target=/root/.m2  mvn -f ${SERVICE_NAME}/pom.xml dependency:go-offline -B

COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src
RUN --mount=type=cache,target=/root/.m2  mvn -f ${SERVICE_NAME}/pom.xml clean package -DskipTests



FROM eclipse-temurin:25-jdk
ARG SERVICE_NAME
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/${SERVICE_NAME}/target/*-SNAPSHOT.jar app.jar
COPY ${SERVICE_NAME}/.env*.properties ./
COPY ${SERVICE_NAME}/*.pem ./

ENV SERVER_PORT=8080
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh" ,"-c", "java $JAVA_OPTS -jar app.jar"]