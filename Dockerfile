FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system mohae && useradd --system --gid mohae --home-dir /app mohae

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

RUN chown -R mohae:mohae /app

USER mohae

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
