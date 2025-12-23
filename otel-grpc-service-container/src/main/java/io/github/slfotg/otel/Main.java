package io.github.slfotg.otel;

import java.io.IOException;

import dagger.grpc.server.NettyServerModule;
import io.grpc.Server;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class Main {
    public static void main(String[] args) {
        int port = 8888;
        HelloComponent component = DaggerHelloComponent.builder()
                .nettyServerModule(NettyServerModule.bindingToPort(port))
                .build();
        Server server = component.server();

        try {
            server.start();
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}