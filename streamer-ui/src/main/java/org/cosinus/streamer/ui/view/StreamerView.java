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

package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.api.pack.PackStreamer;
import org.cosinus.streamer.ui.action.LoadStreamerAction;
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.execute.WorkerListener;
import org.cosinus.streamer.ui.action.execute.save.SaveWorkerModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.form.Panel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.util.Optional.ofNullable;

public abstract class StreamerView<T> extends Panel implements WorkerListener<LoadWorkerModel<T>> {

    protected final String id;

    protected final PanelLocation location;

    protected final JPanel streamerViewMainPanel;

    protected LoadingProgress loadingIndicator;

    @Autowired
    protected StreamerViewHandler streamerViewHandler;

    @Autowired
    protected StreamerHandler streamerHandler;

    @Autowired
    public StreamerViewStorage streamerViewStorage;

    @Autowired
    public ActionController actionController;

    @Autowired
    public AddressBar addressBar;

    protected final Streamer<T> parentStreamer;

    public StreamerView(PanelLocation location, Streamer<T> parentStreamer) {
        this.id = UUID.randomUUID().toString();
        this.location = location;
        this.parentStreamer = parentStreamer;

        streamerViewMainPanel = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
    }

    public String getId() {
        return id;
    }

    public void updateForm() {
    }

    protected LoadingProgress createLoadingIndicator() {
        LoadingProgress loading = new LoadingProgress();
        loading.setIndeterminate(true);
        loading.setPreferredSize(new Dimension(getWidth(), 7));
        return loading;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        this.loadingIndicator = createLoadingIndicator();
        add(streamerViewMainPanel, CENTER);
        add(loadingIndicator, SOUTH);

        updateForm();
    }

    public void loadStreamer(Streamer<T> streamer) {
        loadStreamer(streamer, getSelectedContentIdentifier()
            .orElse(null));
    }

    public void loadStreamer(Streamer<T> streamer, String contentIdentifier) {
        actionController.runAction(LoadStreamerAction.LOAD_STREAMER_ACTION_ID,
                                   new StreamerActionContext<>(streamer, this, contentIdentifier));
    }

    public void reload() {
        loadStreamer(getLoadedStreamer());
    }

    public void reload(String contentIdentifier) {
        loadStreamer(getLoadedStreamer(), contentIdentifier);
    }

    public PanelLocation getCurrentLocation() {
        return location;
    }

    public Streamer<T> getParentStreamer() {
        return parentStreamer;
    }

    @Override
    public void workerStarted(LoadWorkerModel<T> loadWorkerModel)
    {
        loadingIndicator.startLoading(loadWorkerModel.getTotalSizeToLoad());
        updateAddressBarAndStreamerPanel();
    }

    @Override
    public void workerUpdated(LoadWorkerModel<T> loadWorkerModel) {
        streamerViewHandler.getCurrentView().requestFocus();
        loadingIndicator.updateLoading(loadWorkerModel.getLoadedSize(), loadWorkerModel.getTotalSizeToLoad());
    }

    @Override
    public void workerFinished(LoadWorkerModel<T> loadWorkerModel) {
        ofNullable(getLoadedStreamer())
            .filter(streamer -> PackStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(PackStreamer.class::cast)
            .ifPresent(PackStreamer::finishLoading);
        loadingIndicator.finishLoading();

        streamerViewStorage.saveLastLoadedStreamer(getLoadedStreamer(), getCurrentLocation());

        ofNullable(parentStreamer)
            .filter(streamer -> PackStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(PackStreamer.class::cast)
            .ifPresent(PackStreamer::finishLoading);
    }

    public void updateAddressBarAndStreamerPanel() {
        getStreamerAddress().ifPresent(address -> {
            addressBar.setAddress(address);
            getPanel().ifPresent(panel -> {
                panel.setAddress(address);
                ofNullable(getLoadedStreamer())
                    .map(Streamer::getParent)
                    .ifPresent(parent -> panel.setFreeSpace(
                        parent.getFreeSpace(),
                        parent.getTotalSpace()));
            });
        });
    }

    protected Optional<String> getStreamerAddress() {
        return ofNullable(getLoadedStreamer())
            .map(Streamer::getUrlPath)
            .map(address -> address.split("://"))
            .map(address -> address[address.length - 1]);
    }

    public Optional<StreamerPanel> getPanel() {
        return streamerViewHandler.getPanel(getCurrentLocation());
    }

    public boolean isActive() {
        return location == streamerViewHandler.getCurrentLocation();
    }

    public void setActive(boolean active) {
        getPanel().ifPresent(panel -> panel.setEnabled(active));
    }

    public void selectCurrentContent() {
    }

    public void showRename() {
    }

    public void goHome() {
    }

    public void goEnd() {
    }

    public void findContent(String name) {

    }

    public SaveWorkerModel<T> getSaveModel() {
        return null;
    }

    public WorkerListener<? extends SaveWorkerModel<T>> getSaveWorkerListener() {
        return null;
    }

    public abstract String getName();

    public abstract T getCurrentContent();

    public abstract Streamer<T> getLoadedStreamer();

    public abstract List<T> getSelectedContent();

    public abstract Optional<String> getSelectedContentIdentifier();

    public abstract LoadWorkerModel<T> getLoadWorkerModel();
}
