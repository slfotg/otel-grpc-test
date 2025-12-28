package io.github.slfotg.otel.cmd;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.github.slfotg.otel.Cli;
import io.github.slfotg.otel.RssItemStreamObserver;
import io.github.slfotg.otel.grpc.RssFeedGrpc;
import io.github.slfotg.otel.grpc.RssRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "rss", mixinStandardHelpOptions = true, version = "1.0", description = {
        "subscribe to rss events"
}, subcommands = { RssCommand.SubscribeCommand.class, RssCommand.UnsubscribeCommand.class,
        CommandLine.HelpCommand.class })
public class RssCommand implements Runnable {

    ManagedChannel channel;
    RssFeedGrpc.RssFeedStub stub;
    ConcurrentHashMap<String, StreamObserver<RssRequest>> observers = new ConcurrentHashMap<>();

    @ParentCommand
    Cli.CliCommands cliCommands;

    public RssCommand() {
        channel = ManagedChannelBuilder.forAddress("localhost", 8888)
                .usePlaintext()
                .keepAliveTime(100, TimeUnit.DAYS)
                .keepAliveWithoutCalls(true)
                .build();
        stub = RssFeedGrpc.newStub(channel);
    }

    PrintWriter out() {
        return cliCommands.out();
    }

    @Override
    public void run() {
        System.out.println(new CommandLine(this).getUsageMessage());
    }

    @Override
    public void finalize() {
        channel.shutdown();
    }

    @Command(name = "subscribe", mixinStandardHelpOptions = true, version = "1.0", description = {
            "subscribe to rss events"
    }, subcommands = { CommandLine.HelpCommand.class })
    public static class SubscribeCommand implements Runnable {

        @Option(
            names = { "-u", "--url" },
            description = { "The rss feed url" },
            required = true,
            completionCandidates = RssUrls.class
        )
        private String url;

        @ParentCommand
        RssCommand parent;

        @Override
        public void run() {
            parent.observers.computeIfAbsent(url, rssUrl -> {
                StreamObserver<RssRequest> observer = parent.stub.subscribe(new RssItemStreamObserver(parent.out()));
                observer.onNext(RssRequest.newBuilder().setUrl(rssUrl).build());
                return observer;
            });
        }
    }

    @Command(name = "unsubscribe", mixinStandardHelpOptions = true, version = "1.0", description = {
            "unsubscribe to rss events"
    }, subcommands = { CommandLine.HelpCommand.class })
    public static class UnsubscribeCommand implements Runnable {

        @Option(names = { "-u", "--url" }, description = { "The rss feed url" }, required = true)
        private String url;

        @ParentCommand
        RssCommand parent;

        @Override
        public void run() {
            parent.observers.computeIfPresent(url, (rssUrl, observer) -> {
                observer.onCompleted();
                return null;
            });
        }
    }

    public static class RssUrls extends ArrayList<String> {
        public RssUrls() {
            super(Arrays.asList("https://projecteuler.net/rss2_euler.xml"));
        }
    }

}
