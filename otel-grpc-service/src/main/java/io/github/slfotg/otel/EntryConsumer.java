package io.github.slfotg.otel;

import java.util.function.Consumer;

import com.rometools.rome.feed.synd.SyndEntry;

@FunctionalInterface
public interface EntryConsumer extends Consumer<SyndEntry> {

}
