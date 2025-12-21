package io.github.slfotg.otel;

import java.util.Arrays;
import java.util.List;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.grpc.server.ForGrpcService;
import io.github.slfotg.otel.grpc.HelloServiceGrpc;
import io.grpc.ServerInterceptor;

@Module
abstract public class HelloModule {

    @Binds
    abstract HelloServiceDefinition helloServiceDefinition(HelloComponent helloComponent);

    @Provides
    @ForGrpcService(HelloServiceGrpc.class)
    static List<? extends ServerInterceptor> helloInterceptors() {
        return Arrays.asList();
    }
}
