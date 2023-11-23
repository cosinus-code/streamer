package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;

public abstract class ParentStreamerView<S extends Streamer<S>> extends StreamerView<S> {

    public ParentStreamerView(PanelLocation location, ParentStreamer<S> parentStreamer) {
        super(location, parentStreamer);
    }

    @Override
    public ParentStreamer<S> getParentStreamer() {
        return (ParentStreamer<S>) super.getParentStreamer();
    }
}
