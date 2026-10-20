FROM amazoncorretto:27-alpine-jdk AS build
WORKDIR /build
COPY mvnw ./
COPY .mvn .mvn
COPY gateway/pom.xml gateway/pom.xml
COPY gateway/src gateway/src
RUN --mount=type=cache,target=/root/.m2 sh mvnw -B -DskipTests -f gateway/pom.xml package

FROM amazoncorretto:27-alpine-jdk
WORKDIR /app
RUN addgroup -S bookhive && adduser -S -G bookhive bookhive
COPY --from=build /build/gateway/target/*.jar app.jar
USER bookhive
ENV PORT=8080
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s CMD wget -q -O /dev/null http://localhost:8080/puerta/health/liveness || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]
