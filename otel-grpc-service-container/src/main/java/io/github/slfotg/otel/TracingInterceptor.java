package io.github.slfotg.otel;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;

public class TracingInterceptor implements ServerInterceptor {

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("otel-grpc-service");

    static final Metadata.Key<String> CUSTOM_HEADER_KEY = Metadata.Key.of("custom_server_header_key",
            Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        var method = call.getMethodDescriptor().getBareMethodName();
        var attrs = call.getAttributes();
        System.out.println(attrs);
        System.out.println(headers);
        System.out.println(method);

        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
            // @Override
            // public void sendHeaders(Metadata responseHeaders) {
            // System.out.println("Adding headers");
            // responseHeaders.put(CUSTOM_HEADER_KEY, "customRespondValue");
            // super.sendHeaders(responseHeaders);
            // }
        }, headers);
    }

}
