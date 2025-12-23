package io.github.slfotg.otel;

import java.util.HashMap;

import io.github.slfotg.otel.grpc.HelloReply;
import io.github.slfotg.otel.grpc.HelloRequest;
import io.github.slfotg.otel.grpc.HelloRequest.Builder;
import io.github.slfotg.otel.grpc.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;

public class Main {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("otel-grpc-server", 8888)
                .usePlaintext()
                .build();

        OpenTelemetry otel = GlobalOpenTelemetry.get();
        Span span = otel.getTracer("hello-client").spanBuilder("sayHello client").setSpanKind(SpanKind.CLIENT)
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);

            var request = HelloRequest.newBuilder()
                    .setName(HelloRequest.Name.newBuilder()
                            .setFirst("John")
                            .setLast("Doe")
                            .build())
                    .setAge(39);

            otel.getPropagators().getTextMapPropagator().inject(Context.current(), request,
                    RequestBuilderSetter.INSTANCE);

            HelloReply helloResponse = stub.sayHello(request.build());

            System.out.println(helloResponse.getMessage());
        } finally {
            span.end();
        }

        channel.shutdown();
    }

    static class RequestBuilderSetter implements TextMapSetter<HelloRequest.Builder> {

        public static final RequestBuilderSetter INSTANCE = new RequestBuilderSetter();

        @Override
        public void set(Builder builder, String key, String value) {
            builder.putMetadata(key, value);
        }
    }
}