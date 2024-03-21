package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.Streamable;

public interface StreamerEditor<T extends Streamable> {

    void loadAndShow(final T itemToBeEdited);

    void save();

    void setVisible(boolean visible);

    StreamerView<T> getView();
}
