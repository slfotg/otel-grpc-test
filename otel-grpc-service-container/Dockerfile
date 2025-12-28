FROM eclipse-temurin:21.0.9_10-jre-alpine-3.23

ENV OTEL_SERVICE_NAME=hello-service
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://collector:4317
ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_METRICS_EXPORTER=none
ENV OTEL_LOGS_EXPORTER=none
ENV OTEL_JAVAAGENT_ENABLED=true
ENV JAVA_TOOL_OPTIONS="-javaagent:/opentelemetry-javaagent.jar"

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.27.0/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
ADD target/${project.build.finalName}.jar /app.jar
ADD target/lib /lib

CMD ["java", "-cp", "/app.jar:/lib/*", "io.github.slfotg.otel.Main"]
