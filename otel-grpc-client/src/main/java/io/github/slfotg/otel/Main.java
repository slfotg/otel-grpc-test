package io.github.slfotg.otel;

import io.github.slfotg.otel.grpc.HelloReply;
import io.github.slfotg.otel.grpc.HelloRequest;
import io.github.slfotg.otel.grpc.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Main {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("otel-grpc-server", 8888)
                .usePlaintext()
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);

        var request = HelloRequest.newBuilder()
                .setName(HelloRequest.Name.newBuilder()
                        .setFirst("John")
                        .setLast("Doe")
                        .build())
                .setAge(39)
                .build();

        HelloReply helloResponse = stub.sayHello(request);

        System.out.println(helloResponse.getMessage());

        channel.shutdown();
    }
}