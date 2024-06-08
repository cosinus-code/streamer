package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.WorkerModel;

import static java.util.Optional.ofNullable;

public interface LoadWorkerModel<T> extends WorkerModel<T> {

    Streamer<T> getParentStreamer();

    String getContentIdentifier();

    void setContentIdentifier(String contentIdentifier);

    default long getTotalSizeToLoad() {
        return ofNullable(getParentStreamer())
            .map(Streamer::getSize)
            .orElse(-1L);
    }

    default long getLoadedSize() {
        return -1;
    }
}
