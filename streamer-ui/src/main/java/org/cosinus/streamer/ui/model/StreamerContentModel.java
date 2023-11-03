package org.cosinus.streamer.ui.model;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.WorkerModel;

public interface StreamerContentModel<T> extends WorkerModel<T> {

    Streamer<T> getParentStreamer();

    void setParentStreamer(Streamer<T> parentStreamer);

    String getContentIdentifier();

    void setContentIdentifier(String contentIdentifier);
}
