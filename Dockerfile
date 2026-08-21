FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY build-logic ./build-logic
COPY domain/build.gradle.kts domain/build.gradle.kts
COPY feature/ingestion/build.gradle.kts feature/ingestion/build.gradle.kts
COPY feature/enrichment/build.gradle.kts feature/enrichment/build.gradle.kts
COPY feature/analysis/build.gradle.kts feature/analysis/build.gradle.kts
COPY feature/alert/build.gradle.kts feature/alert/build.gradle.kts
COPY app/build.gradle.kts app/build.gradle.kts
COPY test-fixtures/build.gradle.kts test-fixtures/build.gradle.kts
COPY domain/src domain/src
COPY feature/ingestion/src feature/ingestion/src
COPY feature/enrichment/src feature/enrichment/src
COPY feature/analysis/src feature/analysis/src
COPY feature/alert/src feature/alert/src
COPY app/src app/src
COPY test-fixtures/src test-fixtures/src

RUN ./gradlew :app:installDist --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S argus && adduser -S argus -G argus && mkdir -p /data && chown -R argus:argus /data
WORKDIR /app
COPY --from=builder --chown=argus:argus /workspace/app/build/install/app ./

USER argus
EXPOSE 8080
ENV PORT=8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s \
  CMD wget -q -O - http://localhost:${PORT}/health || exit 1

ENTRYPOINT ["sh", "-c", "exec bin/app $JAVA_OPTS"]

