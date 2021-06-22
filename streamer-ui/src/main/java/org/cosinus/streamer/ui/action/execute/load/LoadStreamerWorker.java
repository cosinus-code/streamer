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

package org.cosinus.streamer.ui.action.execute.load;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.pack.PackStreamer;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.streamer.ui.action.progress.SimpleProgressModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * {@link javax.swing.SwingWorker} for loading an streamer
 */
public class LoadStreamerWorker<T> extends SwingWorker<StreamedContent<T>, T> {

    private static final Logger LOG = LogManager.getLogger(LoadStreamerWorker.class);

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private ProgressListenerHandler<SimpleProgressModel> progressListenerHandler;

    private final Streamer<T> streamerToLoad;

    private final StreamerView<T> streamerView;

    private final Streamer<T> contentToSelect;

    private final SimpleProgressModel progress;

    private final List<T> content;

    public LoadStreamerWorker(Streamer<T> streamerToLoad,
                              LoadActionModel loadActionModel) {
        this.streamerToLoad = streamerToLoad;
        this.streamerView = loadActionModel.getView();
        this.contentToSelect = loadActionModel.getContentToSelect();
        this.content = new ArrayList<>();
        this.progress = new SimpleProgressModel(loadActionModel.getActionId());
        updateView();
    }

    @Override
    protected StreamedContent<T> doInBackground() {
        try (Stream<? extends T> contentStream = streamerToLoad.stream()) {
            List<T> content = contentStream
                .peek(this::publish)
                .collect(Collectors.toList());
            return new StreamedContent(streamerToLoad, content, contentToSelect);
        }
    }

    @Override
    protected void process(List<T> chunk) {
        try {
            if (!isCancelled()) {
                content.addAll(chunk);
                updateView();
            }
        } catch (Exception e) {
            LOG.error("Failed to update the view", e);
            errorHandler.handleError(e);
        }
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                ofNullable(get())
                    .filter(streamedContent -> streamedContent.getContent().size() == 0 ||
                        streamedContent.getContent().size() != content.size())
                    .ifPresent(streamerView::updateContent);
                ofNullable(streamerToLoad)
                    .filter(streamer -> PackStreamer.class.isAssignableFrom(streamer.getClass()))
                    .map(PackStreamer.class::cast)
                    .ifPresent(PackStreamer::finishLoading);
            }
        } catch (Exception e) {
            LOG.error("Failed to update the view", e);
            errorHandler.handleError(e);
        } finally {
            progressListenerHandler.finishProgress(progress);
        }
    }

    private void updateView() {
        streamerView.updateContent(new StreamedContent<T>(streamerToLoad, content, contentToSelect));
        progress.updateProgress(content.size());
        progressListenerHandler.setProgress(progress);
    }
}
