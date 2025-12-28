package io.github.slfotg.otel;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.util.HashMap;
import java.util.Map;

import io.github.slfotg.otel.grpc.RssItem;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;

public class TracingInterceptor implements ServerInterceptor {

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
            public void sendMessage(RespT response) {

                if (response instanceof RssItem) {
                    RssItem item = (RssItem) response;
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("test", "value");
                    
                    item = item.toBuilder().putAllMetadata(metadata).build();
                }
                
                System.out.println("send message: " + response);
                super.sendMessage(response);
            }
        }, headers);
    }

}
