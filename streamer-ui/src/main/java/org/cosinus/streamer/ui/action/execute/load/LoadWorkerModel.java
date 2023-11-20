package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.ui.action.execute.WorkerModel;

public interface LoadWorkerModel<T> extends WorkerModel<T> {

    String getContentIdentifier();

    void setContentIdentifier(String contentIdentifier);

    default long getTotalSizeToLoad() {
        return -1;
    }

    default long getLoadedSize() {
        return -1;
    }
}
