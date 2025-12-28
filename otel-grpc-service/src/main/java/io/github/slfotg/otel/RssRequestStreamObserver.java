package io.github.slfotg.otel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import io.github.slfotg.otel.grpc.RssItem;
import io.github.slfotg.otel.grpc.RssRequest;
import io.github.slfotg.otel.mapper.EntryMapper;
import io.github.slfotg.otel.poller.RssPollerManager;
import io.grpc.stub.StreamObserver;

public class RssRequestStreamObserver implements StreamObserver<RssRequest> {
    private final RssPollerManager pollerManager;
    private final StreamObserver<RssItem> responseObserver;
    private final StreamingEntryConsumer consumer;
    private final Set<String> urls;

    public RssRequestStreamObserver(RssPollerManager pollerManager, StreamObserver<RssItem> responseObserver,
            EntryMapper entryMapper) {
        this.pollerManager = pollerManager;
        this.responseObserver = responseObserver;
        this.consumer = new StreamingEntryConsumer(entryMapper, responseObserver);
        this.urls = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void onNext(RssRequest request) {
        String url = request.getUrl();
        System.out.println("subscribing to " + url);
        var poller = pollerManager.getPoller(url, consumer);
        urls.add(url);
        poller.startPolling(10);
    }

    @Override
    public void onError(Throwable t) {
        this.urls.forEach(url -> pollerManager.removeConsumer(url, consumer));
        System.out.println("error");
        t.printStackTrace();
        responseObserver.onError(t);
    }

    @Override
    public void onCompleted() {
        this.urls.forEach(url -> pollerManager.removeConsumer(url, consumer));
        System.out.println("completed");
        responseObserver.onCompleted();
    }
}
