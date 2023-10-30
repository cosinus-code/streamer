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
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.api.pack.PackStreamer;
import org.cosinus.streamer.api.pack.PackerHandler;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.streamer.ui.action.progress.SimpleProgressModel;
import org.cosinus.streamer.ui.view.AddressBar;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.streamer.ui.view.StreamerViewStorage;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * {@link javax.swing.SwingWorker} for loading an streamer
 */
public class LoadStreamerWorker<T> extends SwingWorker<Void, T> {

    private static final Logger LOG = LogManager.getLogger(LoadStreamerWorker.class);

    @Autowired
    private StreamerViewStorage streamerViewStorage;

    @Autowired
    private StreamerHandler streamerHandler;

    @Autowired
    private PackerHandler packerHandler;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private ProgressListenerHandler<SimpleProgressModel> progressListenerHandler;

    @Autowired
    private AddressBar addressBar;

    private Streamer<T> streamerToLoad;

    private final StreamerView<T> streamerView;

    private final String contentIdentifier;

    private final SimpleProgressModel progress;

    private final List<T> content;

    public LoadStreamerWorker(LoadActionModel loadActionModel) {
        this.streamerToLoad = loadActionModel.getStreamer();
        this.streamerView = loadActionModel.getView();
        this.contentIdentifier = loadActionModel.getContentIdentifier();
        this.content = new ArrayList<>();
        this.progress = new SimpleProgressModel(loadActionModel.getActionId());
    }

    @Override
    protected Void doInBackground() {
        streamerToLoad = prepareStreamerToLoad(streamerToLoad);
        if (streamerToLoad == null) {
            LOG.trace("Streamer to load is null -> ignore the command.");
            return null;
        }

        try (Stream<? extends T> contentStream = streamerToLoad.stream()) {
            contentStream.forEach(this::publish);
        }
        return null;
    }

    private Streamer prepareStreamerToLoad(Streamer streamerToLoad) {
        return ofNullable(streamerToLoad)
            .or(this::loadLastStreamer)
            .or(() -> ofNullable(streamerHandler.getDefaultStreamer()))
            .map(this::checkIfStreamerExist)
            .map(this::checkIfStreamerIsPacked)
            .orElse(null);
    }

    private Optional<Streamer> loadLastStreamer() {
        return streamerViewStorage.loadLastLoadedStreamer(streamerView.getCurrentLocation())
                .map(urlPath -> streamerHandler.getStreamer(urlPath));
    }

    private Streamer checkIfStreamerExist(Streamer streamerToCheck) {
        return ofNullable(streamerToCheck)
                .filter(not(Streamer::exists))
                .map(this::getFirstAncestorAlive)
                .orElse(streamerToCheck);
    }

    private Streamer checkIfStreamerIsPacked(Streamer streamerToCheck) {
        return ofNullable(streamerToCheck)
                .filter(streamer -> BinaryStreamer.class.isAssignableFrom(streamer.getClass()))
                .map(BinaryStreamer.class::cast)
                .<Streamer>flatMap(binaryStream -> packerHandler
                        .findPacker(binaryStream.getType())
                        .map(packer -> packer.pack(binaryStream)))
                .orElse(streamerToCheck);
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
                get();
                updateView();
            }
        } catch (Exception e) {
            LOG.error("Failed to update the view", e);
            errorHandler.handleError(e);
        } finally {
            ofNullable(streamerToLoad)
                    .filter(streamer -> PackStreamer.class.isAssignableFrom(streamer.getClass()))
                    .map(PackStreamer.class::cast)
                    .ifPresent(PackStreamer::finishLoading);
            progressListenerHandler.finishProgress(progress);
        }
    }

    private void updateView() {
        if (streamerToLoad != null) {
            streamerView.updateContent(new StreamedContent<>(streamerToLoad, content, contentIdentifier));
            updateAddressBar();
            progress.updateProgress(content.size());
            progressListenerHandler.setProgress(progress);
        }
    }

    private void updateAddressBar() {
        streamerViewHandler.getPanel(streamerView.getCurrentLocation())
                .ifPresent(panel -> ofNullable(streamerToLoad.getUrlPath())
                        .map(address -> address.split("://"))
                        .map(address -> address[address.length - 1])
                        .ifPresent(address -> {
                            addressBar.setAddress(address);
                            panel.setAddress(address);
                            panel.setFreeSpace(streamerToLoad.getParent().getFreeSpace(),
                                    streamerToLoad.getParent().getTotalSpace());
                        }));
    }

    private Streamer getFirstAncestorAlive(Streamer streamer) {
        return ofNullable(streamer.getParent())
                .filter(not(Streamer::exists))
                .map(this::getFirstAncestorAlive)
                .orElse(streamer.getParent());
    }
}
