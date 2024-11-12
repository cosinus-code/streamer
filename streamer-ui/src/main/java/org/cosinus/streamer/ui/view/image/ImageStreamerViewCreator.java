package org.cosinus.streamer.ui.view.image;

import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewCreator;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.view.image.ImageStreamerView.IMAGE_VIEWER;

@Component
public class ImageStreamerViewCreator implements StreamerViewCreator {

    @Override
    public StreamerView<?, ?> createStreamerView(PanelLocation location) {
        return new ImageStreamerView(location);
    }

    @Override
    public String getViewName() {
        return IMAGE_VIEWER;
    }
}
