FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir -p /data/objects
COPY --from=build /workspace/target/dropbucket-0.0.1-SNAPSHOT.jar /app/dropbucket.jar
EXPOSE 8080
ENV DROPBUCKET_DB_PATH=/data/dropbucket.sqlite
ENV DROPBUCKET_OBJECT_DIR=/data/objects
ENTRYPOINT ["java", "-jar", "/app/dropbucket.jar"]
