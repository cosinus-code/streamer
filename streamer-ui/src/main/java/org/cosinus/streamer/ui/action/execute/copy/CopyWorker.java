/*
 * Copyright 2020 Cosinus Software
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

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.ContainerStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.SwingProgressWorker;
import org.cosinus.streamer.ui.action.progress.CopyProgressModel;
import org.cosinus.streamer.ui.error.ActionException;
import org.cosinus.swing.dialog.DialogHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * {@link SwingProgressWorker} for copying streamers from a source container to target container
 */
public class CopyWorker<S extends Streamer<?>, T extends Streamer<?>> extends SwingProgressWorker<CopyProgressModel> {

    @Autowired
    protected DialogHandler dialogHandler;

    private final CopyActionModel<S, T> copyModel;

    private final ContainerStreamer<S> source;

    private final ContainerStreamer<T> destination;

    public CopyWorker(CopyActionModel<S, T> copyModel,
                      Window parentWindow) {
        super(parentWindow, copyModel.getActionId(), new CopyProgressModel(copyModel.getActionId()));
        this.copyModel = copyModel;
        this.source = copyModel.getSource();
        this.destination = copyModel.getDestination();
    }

    @Override
    protected void doWork() {
        try {
            new CopyPipeline<>(copyModel, this).consume();
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, "act_copy_error", source.getPath(), destination.getPath());
        }
    }
}
