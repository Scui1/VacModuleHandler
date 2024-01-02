FROM gradle:jdk17 AS build
# For PEFile library dependency:
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN
COPY --chown=gradle:gradle build.gradle.kts gradle.properties settings.gradle.kts /home/gradle/src/
RUN mkdir /home/gradle/src/src
COPY src /home/gradle/src/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:17-jre
RUN mkdir -p /app/storage
COPY --from=build /home/gradle/src/build/libs/vacmodulehandler.jar /app/vacmodulehandler.jar
EXPOSE 8083:8083
CMD ["java", "-jar", "/app/vacmodulehandler.jar", "-port=8083", "-P:knownModulesFilePath=/app/storage/knowModules.json"]
