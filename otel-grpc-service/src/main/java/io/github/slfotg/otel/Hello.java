package io.github.slfotg.otel;

import dagger.grpc.server.GrpcService;
import io.github.slfotg.otel.grpc.HelloReply;
import io.github.slfotg.otel.grpc.HelloRequest;
import io.github.slfotg.otel.grpc.HelloServiceGrpc;
import io.github.slfotg.otel.grpc.HelloServiceGrpc.HelloServiceImplBase;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@GrpcService(grpcClass = HelloServiceGrpc.class)
public class Hello extends HelloServiceImplBase {

    // private final Tracer tracer;

    @Inject
    public Hello() {
        // this.tracer = GlobalOpenTelemetry.getTracer("otel-grpc-service");
    }

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        Span span = GlobalOpenTelemetry.getTracer("server").spanBuilder("got message").setSpanKind(SpanKind.SERVER)
                .startSpan();
        Context context = GlobalOpenTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(),
                request, HelloRequestTextMapGetter.INSTANCE);
        context.makeCurrent();
        var firstName = request.getName().getFirst();
        var lastName = request.getName().getLast();
        var age = request.getAge();
        String message = "Hello %s %s (age: %d)".formatted(firstName, lastName, age);
        responseObserver
                .onNext(HelloReply.newBuilder().setMessage(message).build());
        responseObserver.onCompleted();
        span.end();
        // } finally {
        // span.end();
        // }
    }

    static class HelloRequestTextMapGetter implements TextMapGetter<HelloRequest> {

        public static final HelloRequestTextMapGetter INSTANCE = new HelloRequestTextMapGetter();

        @Override
        public String get(HelloRequest request, String key) {
            System.out.println(key + " : " + request.getMetadataMap());
            return request.getMetadataOrDefault(key, "");
        }

        @Override
        public Iterable<String> keys(HelloRequest request) {
            return request.getMetadataMap().keySet();
        }
    }
}
