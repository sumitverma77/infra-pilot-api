FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -DskipTests package

FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=build /workspace/target/*-exec.jar /app/app.jar

ARG COMMIT_SHA=unknown
ENV INFRAPILOT_GIT_COMMIT_SHA=${COMMIT_SHA}
ARG APP_VERSION=unknown
ENV INFRAPILOT_APP_VERSION=${APP_VERSION}
ARG BUILD_TIMESTAMP=unknown
ENV INFRAPILOT_BUILD_TIMESTAMP=${BUILD_TIMESTAMP}

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
