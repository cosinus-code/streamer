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
import org.cosinus.streamer.ui.action.LoadStreamerAction;
import org.cosinus.streamer.ui.action.ReloadStreamerAction;
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.execute.load.StreamedContent;
import org.cosinus.streamer.ui.action.progress.ProgressListener;
import org.cosinus.streamer.ui.action.progress.SimpleProgressModel;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.form.Panel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.UUID;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;

public abstract class StreamerView<T> extends Panel implements ProgressListener<SimpleProgressModel> {

    protected final String id;

    protected final PanelLocation location;

    protected final JPanel panContent;

    protected Component loadingIndicator;

    @Autowired
    protected StreamerViewHandler streamerViewHandler;

    @Autowired
    protected StreamerHandler streamerHandler;

    @Autowired
    public StreamerViewStorage streamerViewStorage;

    @Autowired
    public ActionController actionController;

    public StreamerView(PanelLocation location) {
        this.id = UUID.randomUUID().toString();
        this.location = location;

        panContent = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
    }

    public String getId() {
        return id;
    }

    public void updateForm() {
    }

    protected Component createLoadingIndicator() {
        JProgressBar loading = new JProgressBar();
        loading.setIndeterminate(true);
        loading.setPreferredSize(new Dimension(getWidth(), 10));

        return loading;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        this.loadingIndicator = createLoadingIndicator();
        add(panContent, CENTER);
        add(loadingIndicator, SOUTH);

        updateForm();
    }

    public void loadStreamer(Streamer<T> streamer) {
        actionController.runAction(LoadStreamerAction.LOAD_ELEMENT_ACTION_ID,
                                   new StreamerActionContext<>(streamer, this));
    }

    public void reload() {
        actionController.runAction(ReloadStreamerAction.RELOAD_ELEMENT_ACTION_ID);
    }

    public PanelLocation getCurrentLocation() {
        return location;
    }

    @Override
    public void startProgress() {
        loadingIndicator.setVisible(true);
    }

    @Override
    public void setProgress(SimpleProgressModel progressModel) {
    }

    @Override
    public void finishProgress() {
        loadingIndicator.setVisible(false);
        streamerViewStorage.saveLastLoadedStreamer(getLoadedStreamer(), getCurrentLocation());
    }

    public boolean isActive() {
        return location == streamerViewHandler.getCurrentLocation();
    }

    public void updateContent(StreamedContent<T> content) {
        internalUpdateContent(content);
        if (!isActive()) {
            setActive(false);
        } else {
            //TODO: to investigate why this is needed after delete
            requestFocus();
        }
    }

    public void showRename() {
    }

    public abstract void setActive(boolean active);

    protected abstract void internalUpdateContent(StreamedContent<T> content);

    public abstract void selectCurrentContent();

    public abstract void findContent(String name);

    public abstract T getCurrentContent();

    public abstract Streamer<T> getLoadedStreamer();

    public abstract Rectangle getCurrentRectangle();

    public abstract void goHome();

    public abstract void goEnd();

    public abstract List<T> getSelectedContent();
}
