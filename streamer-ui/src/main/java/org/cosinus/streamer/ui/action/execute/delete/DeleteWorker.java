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

package org.cosinus.streamer.ui.action.execute.delete;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.SwingProgressWorker;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import org.cosinus.streamer.ui.error.ActionCancelledException;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.stream.Stream;

/**
 * {@link SwingProgressWorker} for deleting streamers
 */
public class DeleteWorker extends SwingProgressWorker<StreamersProgressModel> {

    @Autowired
    private Translator translator;

    private final DeleteActionModel deleteModel;

    public DeleteWorker(Window parentWindow,
                        DeleteActionModel deleteModel) {
        super(parentWindow,
              deleteModel.getActionId(),
              new StreamersProgressModel(deleteModel.getActionId()));
        this.deleteModel = deleteModel;
    }

    @Override
    protected void doWork() {

        long streamersToDeleteCount = deleteModel
            .getStreamer()
            .count(deleteModel.getStreamerFilter());

        progress.startProgress(streamersToDeleteCount);
        setProgress(progress);

        deleteModel.getStreamersToDelete()
            .forEach(this::delete);
    }

    private void delete(Streamer streamToDelete) {
        if (isCancelled()) {
            throw new ActionCancelledException();
        }

        if (streamToDelete.isDirectory()) {
            try (Stream<? extends Streamer> streamers = streamToDelete.stream()) {
                streamers.forEach(this::delete);
            }
        }

        progress.updateProgress(streamToDelete);
        setProgress(progress);

        if (!streamToDelete.delete()) {
            dialogHandler.showInfo(translator.translate("act-delete-cannot", streamToDelete.getPath()));
        }
    }
}
