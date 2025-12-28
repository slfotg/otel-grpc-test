package io.github.slfotg.otel;

import java.util.HashMap;
import java.util.Map;

import com.rometools.rome.feed.synd.SyndEntry;

import io.github.slfotg.otel.grpc.RssItem;
import io.github.slfotg.otel.mapper.EntryMapper;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

public class StreamingEntryConsumer implements EntryConsumer {

    private final EntryMapper mapper;
    private final StreamObserver<RssItem> responseObserver;

    public StreamingEntryConsumer(EntryMapper mapper, StreamObserver<RssItem> responseObserver) {
        this.mapper = mapper;
        this.responseObserver = responseObserver;
    }

    @Override
    @WithSpan
    public void accept(@SpanAttribute("entry") SyndEntry entry) {
        TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
        Map<String, String> metadata = new HashMap<>();
        propagator.inject(Context.current(), metadata, new TextMapSetter<Map<String, String>>() {

            @Override
            public void set(Map<String, String> metadata, String key, String value) {
                metadata.put(key, value);
            }

        });
        var item = mapper.toItem(entry);
        item.putAllMetadata(metadata);
        System.out.println(item);
        responseObserver.onNext(item.build());
    }

}
