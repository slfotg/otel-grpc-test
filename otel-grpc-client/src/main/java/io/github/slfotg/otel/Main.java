package io.github.slfotg.otel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.github.slfotg.otel.grpc.RssFeedGrpc;
import io.github.slfotg.otel.grpc.RssRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("main created");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("otel-grpc-server", 8888)
                .usePlaintext()
                .keepAliveTime(100, TimeUnit.DAYS)
                .keepAliveWithoutCalls(true)
                .build();

        ExecutorService service = Executors.newWorkStealingPool();

        System.out.println("channel created");
        RssFeedGrpc.RssFeedStub stub = RssFeedGrpc.newStub(channel).withExecutor(service);
        System.out.println("stub created");

        var observer = (ClientCallStreamObserver<RssRequest>) stub.subscribe(new RssItemStreamObserver());

        observer.onNext(RssRequest.newBuilder().setUrl("https://projecteuler.net/rss2_euler.xml").build());

        System.out.println("subscribe created");

        var response = RssFeedGrpc.newBlockingStub(channel)
                .test(RssRequest.newBuilder().setUrl("https://projecteuler.net/rss2_euler.xml").build());
        System.out.println(response);

        channel.awaitTermination(10, TimeUnit.DAYS);
        channel.shutdown();
        System.out.println("channel shutdown");
    }
}