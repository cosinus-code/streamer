package org.cosinus.streamer.api.meta;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.cosinus.streamer.api.meta.MetaStreamer.META_PROTOCOL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class MetaStreamerTest {

    private final MetaStreamer metaStreamer = new MetaStreamer(List.of());

    @Test
    public void findByItsOwnUrlPathReturnsTheMetaStreamer() {
        assertEquals(META_PROTOCOL, metaStreamer.getUrlPath());
        assertSame(metaStreamer, metaStreamer.findByUrlPath(metaStreamer.getUrlPath()).orElse(null));
    }

    @Test
    public void findByUnknownUrlPathIsEmpty() {
        assertEquals(Optional.empty(), metaStreamer.findByUrlPath(META_PROTOCOL + "unknown"));
        assertEquals(Optional.empty(), metaStreamer.findByUrlPath("file:///tmp"));
    }
}
