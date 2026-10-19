FROM amazoncorretto:27-alpine-jdk AS build
WORKDIR /build
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
RUN --mount=type=cache,target=/root/.m2 sh mvnw -B -DskipTests package

FROM amazoncorretto:27-alpine-jdk
WORKDIR /app
RUN addgroup -S bookhive && adduser -S -G bookhive bookhive
COPY --from=build /build/target/*.jar app.jar
USER bookhive
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -q -O /dev/null http://localhost:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]