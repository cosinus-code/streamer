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
package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

public abstract class DefaultStreamerView<T extends Streamable> extends StreamerView<T, T> {

    protected StreamerEditor<T> streamerEditor;

    public DefaultStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void showDetailEditors() {
        ofNullable(getCurrentItem())
            .filter(not(this.getParentStreamer()::equals))
            .ifPresent(currentStream -> {
                if (!currentStream.canUpdate()) {
                    dialogHandler.showInfo(translator.translate("act-new-cannot-update", currentStream.getPath()));
                } else {
                    streamerEditor.loadAndShow(currentStream);
                }
            });
    }

    @Override
    public void reset(final Streamer<T> parentStreamer) {
        super.reset(parentStreamer);
        this.streamerEditor = createStreamerEditor();
    }

    protected abstract StreamerEditor<T> createStreamerEditor();
}
