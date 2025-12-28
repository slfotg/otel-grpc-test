package io.github.slfotg.otel;

import dagger.grpc.server.GrpcService;
import io.github.slfotg.otel.grpc.RssFeedGrpc;
import io.github.slfotg.otel.grpc.RssItem;
import io.github.slfotg.otel.grpc.RssRequest;
import io.github.slfotg.otel.grpc.TestResponse;
import io.github.slfotg.otel.mapper.EntryMapper;
import io.github.slfotg.otel.poller.RssPollerManager;
import io.grpc.stub.StreamObserver;

import javax.inject.Inject;

@GrpcService(grpcClass = RssFeedGrpc.class)
public class RssFeedService extends RssFeedGrpc.RssFeedImplBase {

    private final EntryMapper mapper;
    private final RssPollerManager pollerManager;

    @Inject
    public RssFeedService(EntryMapper mapper, RssPollerManager pollerManager) {
        this.mapper = mapper;
        this.pollerManager = pollerManager;
    }

    // @WithSpan
    @Override
    public StreamObserver<RssRequest> subscribe(StreamObserver<RssItem> responseObserver) {
        return new RssRequestStreamObserver(pollerManager, responseObserver, mapper);
    }

    public void test(RssRequest request, StreamObserver<TestResponse> responseObserver) {
        responseObserver.onNext(TestResponse.newBuilder().setMessage("Hello sir").build());
        responseObserver.onCompleted();
    }
}
