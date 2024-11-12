package org.cosinus.streamer.ui.action.execute.load.image;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.image.ImageStreamerView;
import org.cosinus.swing.action.execute.ActionModel;

public class LoadImageActionModel extends ActionModel {

    public static final String LOAD_IMAGE_ACTION_ID = "load-streamer";

    private final BinaryStreamer streamerToLoad;

    private final ImageStreamerView imageStreamerView;

    public LoadImageActionModel(final BinaryStreamer streamerToLoad,
                                final ImageStreamerView imageStreamerView) {
        super(imageStreamerView.getCurrentLocation().name(), LOAD_IMAGE_ACTION_ID);
        this.streamerToLoad = streamerToLoad;
        this.imageStreamerView = imageStreamerView;
    }

    public BinaryStreamer getStreamerToLoad() {
        return streamerToLoad;
    }

    public ImageStreamerView getImageStreamerView() {
        return imageStreamerView;
    }
}
