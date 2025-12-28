package io.github.slfotg.otel;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

import io.github.slfotg.otel.grpc.RssItem;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.instrumentation.annotations.WithSpan;

public class RssItemStreamObserver implements StreamObserver<RssItem> {

    private final PrintWriter out;

    ContextPropagators propagators = ContextPropagators.create(
            TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance()));

    public RssItemStreamObserver(PrintWriter out) {
        this.out = out;
    }

    public RssItemStreamObserver(PrintStream out) {
        this(new PrintWriter(out));
    }

    public RssItemStreamObserver() {
        this(System.out);
    }

    @Override
    public void onNext(RssItem item) {
        TextMapPropagator propagator = propagators.getTextMapPropagator();
        Map<String, String> metadata = item.getMetadataMap();
        System.out.println(Context.current());
        Context parent = propagator.extract(Context.current(), metadata, new TextMapGetter<Map<String, String>>() {

            @Override
            public String get(Map<String, String> metadata, String key) {
                var value = metadata.get(key);
                out.println(key + " : " + value);
                return value;
            }

            @Override
            public Iterable<String> keys(Map<String, String> arg0) {
                return metadata.keySet();
            }

        });
        Span child = GlobalOpenTelemetry.getTracer("application")
                .spanBuilder("Observer.onNext")
                .setAttribute("item.id", item.getGuid())
                .setAttribute("item.link", item.getLink())
                .setAttribute("item.title", item.getTitle())
                .setParent(parent)
                .startSpan();
        try (Scope ignore = child.makeCurrent()) {
            printItem(item);
        } finally {
            child.end();
        }
    }

    @WithSpan
    private void printItem(RssItem item) {
        out.println("Item: " + item);
    }

    @Override
    public void onError(Throwable t) {
        out.println("error happened");
    }

    @Override
    @WithSpan
    public void onCompleted() {
        out.println("completed");
    }

}
