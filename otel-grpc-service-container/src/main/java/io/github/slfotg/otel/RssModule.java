package io.github.slfotg.otel;

import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

import org.mapstruct.factory.Mappers;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.grpc.server.ForGrpcService;
import io.github.slfotg.otel.grpc.RssFeedGrpc;
import io.github.slfotg.otel.mapper.EntryMapper;
import io.grpc.ServerInterceptor;

@Module
abstract public class RssModule {

    @Binds
    abstract RssFeedServiceServiceDefinition rssFeedServiceServiceDefinition(RssComponent rssComponent);

    @Provides
    @Singleton
    static EntryMapper mapper() {
        return Mappers.getMapper(EntryMapper.class);
    }

    @Provides
    @ForGrpcService(RssFeedGrpc.class)
    static List<? extends ServerInterceptor> helloInterceptors() {
        return Arrays.asList();
    }
}
