package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.WorkerModel;

import static java.util.Optional.ofNullable;

public interface LoadWorkerModel<T, V> extends WorkerModel<V> {

    Streamer<T> getParentStreamer();

    default String getContentIdentifier() {
        return null;
    }

    default void setContentIdentifier(String contentIdentifier) {
    }

    default long getTotalSizeToLoad() {
        return ofNullable(getParentStreamer())
            .map(Streamer::getSize)
            .orElse(-1L);
    }

    default long getLoadedSize() {
        return -1;
    }
}
