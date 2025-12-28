package io.github.slfotg.otel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.rometools.rome.feed.synd.SyndEntry;

import io.github.slfotg.otel.grpc.RssItem;

@Mapper(componentModel = MappingConstants.ComponentModel.JSR330)
public interface EntryMapper {

    default RssItem.Builder toItem(SyndEntry entry) {
        if ( entry == null ) {
            return null;
        }
        RssItem.Builder rssItem = RssItem.newBuilder();

        rssItem.setDescription( entry.getDescription().getValue() );
        rssItem.setGuid( entry.getUri() );
        rssItem.setTitle( entry.getTitle() );
        rssItem.setLink( entry.getLink() );

        return rssItem;
    }

}
