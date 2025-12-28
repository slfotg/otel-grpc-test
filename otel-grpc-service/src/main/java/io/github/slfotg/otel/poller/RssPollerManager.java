package io.github.slfotg.otel.poller;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.github.slfotg.otel.EntryConsumer;

@Singleton
public class RssPollerManager {

    private final ConcurrentHashMap<String, RssPoller> pollerMap;

    @Inject
    public RssPollerManager() {
        pollerMap = new ConcurrentHashMap<>();
    }

    public RssPoller getPoller(String url, EntryConsumer entryConsumer) {
        var poller = pollerMap.computeIfAbsent(url, RssPoller::new);
        poller.addConsumer(entryConsumer);
        System.out.println("pollerMap length: " + pollerMap.size());
        return poller;
    }

    public void removeConsumer(String url, EntryConsumer entryConsumer) {
        pollerMap.computeIfPresent(url, (pollerUrl, poller) -> {
            if (poller.removeConsumer(entryConsumer) && !poller.hasConsumers()) {
                System.out.println("poller removed");
                poller.stopPolling();
                return null;
            }
            return poller;
        });
        System.gc();
        System.out.println("pollerMap length: " + pollerMap.size());
    }
}
