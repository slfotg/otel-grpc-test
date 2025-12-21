package io.github.slfotg.otel;

import dagger.grpc.server.GrpcService;
import io.github.slfotg.otel.grpc.HelloReply;
import io.github.slfotg.otel.grpc.HelloRequest;
import io.github.slfotg.otel.grpc.HelloServiceGrpc;
import io.github.slfotg.otel.grpc.HelloServiceGrpc.HelloServiceImplBase;
import io.grpc.stub.StreamObserver;
import javax.inject.Inject;

@GrpcService(grpcClass = HelloServiceGrpc.class)
public class Hello extends HelloServiceImplBase {

    @Inject
    public Hello() {
    }

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        var firstName = request.getName().getFirst();
        var lastName = request.getName().getLast();
        var age = request.getAge();
        String message = "Hello %s %s (age: %d)".formatted(firstName, lastName, age);
        responseObserver
                .onNext(HelloReply.newBuilder().setMessage(message).build());
        responseObserver.onCompleted();
    }
}
