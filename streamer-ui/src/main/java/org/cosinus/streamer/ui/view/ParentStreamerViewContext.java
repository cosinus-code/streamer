package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import error.StreamerException;

import java.util.List;

public class ParentStreamerViewContext<T extends Streamer<T>> {

    private final ParentStreamer<T> parentStreamer;

    private final Streamer<T> currentItem;

    private final List<Streamer<T>> selectedItems;

    public ParentStreamerViewContext(final StreamerView<T, T> streamerView) {
        if (!streamerView.getParentStreamer().isParent()) {
            throw new StreamerException("Cannot create parent streamer context for a non parent streamer");
        }
        this.parentStreamer = (ParentStreamer<T>) streamerView.getParentStreamer();
        this.currentItem = streamerView.getCurrentItem();
        this.selectedItems = (List<Streamer<T>>) streamerView.getSelectedItems();
    }

    public ParentStreamer<T> getParentStreamer() {
        return parentStreamer;
    }

    public Streamer<T> getCurrentItem() {
        return currentItem;
    }

    public List<Streamer<T>> getSelectedItems() {
        return selectedItems;
    }
}
