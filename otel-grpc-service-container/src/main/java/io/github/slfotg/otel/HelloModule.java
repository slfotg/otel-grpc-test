package io.github.slfotg.otel;

import java.util.Arrays;
import java.util.List;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.grpc.server.ForGrpcService;
import io.github.slfotg.otel.grpc.HelloServiceGrpc;
import io.grpc.ServerInterceptor;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;

@Module
abstract public class HelloModule {

    @Binds
    abstract HelloServiceDefinition helloServiceDefinition(HelloComponent helloComponent);

    @Provides
    @ForGrpcService(HelloServiceGrpc.class)
    static List<? extends ServerInterceptor> helloInterceptors() {
        // OpenTelemetry otel = GlobalOpenTelemetry.get();
        // GrpcTelemetry grpcTelemetry = GrpcTelemetry.create(otel);
        return Arrays.asList();
    }
}
