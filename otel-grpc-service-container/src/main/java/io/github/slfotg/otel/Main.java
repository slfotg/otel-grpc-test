package io.github.slfotg.otel;

import java.io.IOException;

import dagger.grpc.server.NettyServerModule;
import io.grpc.Server;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;

public class Main {

    public static void main(String[] args) {
        int port = 8888;
        OpenTelemetry openTelemetry = GlobalOpenTelemetry.getOrNoop();
        System.out.println(openTelemetry);

        Server server = null;
        Span span = GlobalOpenTelemetry.getTracer("application").spanBuilder("Main.main").setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        try (var ignore = span.makeCurrent()) {
            RssComponent component = DaggerRssComponent.builder()
                    .nettyServerModule(NettyServerModule.bindingToPort(port))
                    .build();
            server = component.server();
        } finally {
            span.end();
        }

        try {
            server.start();
            System.out.println("server started");
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}