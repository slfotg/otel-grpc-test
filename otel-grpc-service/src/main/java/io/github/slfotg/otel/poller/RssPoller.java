package io.github.slfotg.otel.poller;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import io.github.slfotg.otel.EntryConsumer;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;

public class RssPoller {
    private final HttpClient client = HttpClient.newBuilder().build();
    private final String url;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Set<String> seenIds = ConcurrentHashMap.newKeySet();
    private final Set<EntryConsumer> entryConsumers = ConcurrentHashMap.newKeySet();
    private volatile String etag;
    private volatile String lastModified;
    private ScheduledFuture<?> future = null;

    public RssPoller(String url) {
        System.out.println("New poller for " + url);
        this.url = url;
    }

    public void addConsumer(EntryConsumer entryConsumer) {
        this.entryConsumers.add(entryConsumer);
        System.out.println("added consumers: " + this.entryConsumers.size());
    }

    public boolean removeConsumer(EntryConsumer entryConsumer) {
        var removed = this.entryConsumers.remove(entryConsumer);
        System.out.println("removed consumers: " + this.entryConsumers.size());
        return removed;
    }

    public boolean hasConsumers() {
        return !this.entryConsumers.isEmpty();
    }

    public synchronized void startPolling(long intervalSeconds) {
        if (future == null) {
            System.out.println("starting polling on " + url);
            future = scheduler.scheduleAtFixedRate(this::poll, 2, intervalSeconds, TimeUnit.SECONDS);
        }
    }

    public synchronized void stopPolling() {
        if (future != null) {
            System.out.println("stopping polling on " + url);
            future.cancel(true);
            future = null;
        }
    }

    private void poll() {
        try {
            System.out.println("polling...");
            HttpRequest.Builder reqB = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/rss+xml, application/xml, text/xml");
            if (etag != null)
                reqB.header("If-None-Match", etag);
            if (lastModified != null)
                reqB.header("If-Modified-Since", lastModified);

            HttpRequest req = reqB.GET().build();
            HttpResponse<InputStream> res = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

            if (res.statusCode() == 304) {
                System.out.println("not modified");
                return; // not modified
            }

            // store conditional headers for next poll
            res.headers().firstValue("etag").ifPresent(v -> etag = v);
            res.headers().firstValue("last-modified").ifPresent(v -> lastModified = v);

            try (XmlReader reader = new XmlReader(res.body())) {
                sendEntries(new SyndFeedInput().build(reader));
            }
        } catch (Exception e) {
            // handle/log with backoff if needed
            e.printStackTrace();
        }
    }

    @WithSpan
    private void sendEntries(SyndFeed feed) {
        for (SyndEntry entry : feed.getEntries()) {
            String id = entry.getUri() != null ? entry.getUri()
                    : entry.getLink() + "|" + entry.getPublishedDate();
            System.out.println("Adding id: " + id);
            if (seenIds.add(id)) {
                entryConsumers.stream().forEach(consumer -> consumer.accept(entry));
            }
        }
    }

    // for testing purposes:
    @Override
    public void finalize() {
        System.out.println("Finalize called");
    }
}