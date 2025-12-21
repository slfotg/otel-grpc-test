package io.github.slfotg.otel;

import io.grpc.Server;
import javax.inject.Singleton;

import dagger.Component;
import dagger.grpc.server.NettyServerModule;

@Singleton
@Component(modules = {
        NettyServerModule.class,
        HelloUnscopedGrpcServiceModule.class,
        HelloModule.class,
})
interface HelloComponent extends HelloServiceDefinition {
    Server server();
}
