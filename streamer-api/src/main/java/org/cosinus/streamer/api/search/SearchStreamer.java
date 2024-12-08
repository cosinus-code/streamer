package org.cosinus.streamer.api.search;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.TextStreamer;
import org.cosinus.swing.find.FindText;
import org.cosinus.swing.find.TextFinder;
import org.cosinus.swing.text.TextHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.api.StreamerFilter.ALL_STREAMERS;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.springframework.util.MimeTypeUtils.ALL_VALUE;

public class SearchStreamer<S extends Streamer<S>> implements ParentStreamer<FoundStreamer<S>> {

    public static final String SEARCH_PROTOCOL = "find://";

    @Autowired
    private TextHandler textHandler;

    private final ParentStreamer<S> currentStreamer;

    private final StreamerFilter streamerFilter;

    private final boolean deepSearch;

    private final FindText streamerContetFindText;

    private List<FoundStreamer<S>> foundStreamers;

    private long searchTotalCount = -1;

    private final AtomicLong searchDoneCount;

    public SearchStreamer(final ParentStreamer<S> currentStreamer,
                          final FindText streamerNameFindText,
                          final FindText streamerContetFindText,
                          boolean deepSearch) {
        injectContext(this);
        this.currentStreamer = currentStreamer;
        this.streamerFilter = ofNullable(streamerNameFindText)
            .filter(findText -> !findText.getText().isBlank())
            .filter(findText -> !findText.getText().equals(ALL_VALUE))
            .<StreamerFilter>map(findText -> streamer -> textHandler
                .createTextFinder(streamer.getName(), findText)
                .containsText())
            .orElse(ALL_STREAMERS);
        this.streamerContetFindText = streamerContetFindText;
        this.deepSearch = deepSearch;
        this.searchDoneCount = new AtomicLong();
    }

    @Override
    public Stream<FoundStreamer<S>> stream() {
        if (foundStreamers != null) {
            return foundStreamers.stream();
        }

        searchTotalCount = ofNullable(streamerContetFindText)
            .filter(findText -> !findText.getText().isBlank())
            .map(findText -> internalStream()
                .filter(streamerFilter)
                .filter(Streamer::isTextCompatible)
                .count())
            .orElse(-1L);

        foundStreamers = new ArrayList<>();
        return internalStream()
            .filter(streamerFilter)
            .filter(streamer -> ofNullable(streamerContetFindText)
                .filter(findText -> !findText.getText().isBlank())
                .map(findText -> containsText(streamer, findText))
                .orElse(true))
            .map(this::createFindStreamer)
            .peek(foundStreamers::add);
    }

    private Stream<S> internalStream() {
        return deepSearch ? currentStreamer.flatStream() : currentStreamer.stream();
    }

    private FoundStreamer<S> createFindStreamer(S streamer) {
        return new FoundStreamer<>(streamer, this);
    }

    private boolean containsText(final Streamer<S> streamer, final FindText findText) {
        searchDoneCount.incrementAndGet();
        return Optional.of(streamer)
            .filter(Streamer::isTextCompatible)
            .map(Streamer::binaryStreamer)
            .map(TextStreamer::new)
            .map(TextStreamer::stream)
            .orElseGet(Stream::empty)
            .map(text -> textHandler.createTextFinder(text, findText))
            .anyMatch(TextFinder::containsText);
    }

    @Override
    public Path getPath() {
        return currentStreamer.getPath();
    }

    @Override
    public String getProtocol() {
        return SEARCH_PROTOCOL;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return currentStreamer;
    }

    @Override
    public void reset() {
        foundStreamers = null;
    }

    @Override
    public long getSize() {
        return foundStreamers.size();
    }

    public long getSearchTotalCount() {
        return searchTotalCount;
    }

    public long getSearchDoneCount() {
        return searchDoneCount.get();
    }
}
