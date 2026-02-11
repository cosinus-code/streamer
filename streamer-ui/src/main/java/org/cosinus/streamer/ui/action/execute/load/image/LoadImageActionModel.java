/*
 * Copyright 2025 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.ui.action.execute.load.image;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.image.ImageStreamerView;
import org.cosinus.swing.action.execute.ActionModel;

public class LoadImageActionModel extends ActionModel {

    public static final String LOAD_IMAGE_ACTION_ID = "load-image-streamer";

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
