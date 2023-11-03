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
import org.cosinus.streamer.ui.model.StreamerContentModel;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.color.SystemColor.MENU_SELECTION_BACKGROUND;

public abstract class StreamerView<T> extends Panel implements WorkerListener<StreamerContentModel<T>>
{

    protected final String id;

    protected final PanelLocation location;

    protected final JPanel panContent;

    protected Component loadingIndicator;

    @Autowired
    private ApplicationUIHandler uiHandler;

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
        JProgressBar loading = new JProgressBar(0, 100);
        loading.setIndeterminate(true);
        loading.setPreferredSize(new Dimension(getWidth(), 7));
        loading.setUI(new BasicProgressBarUI() {
            @Override
            protected void paintIndeterminate(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                Rectangle rectangle = new Rectangle();
                getBox(rectangle);
                g.setColor(uiHandler.getColor(MENU_SELECTION_BACKGROUND));
                g.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height - 1, 10, 10);
            }
        });

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

    @Override
    public void workerStarted() {
        loadingIndicator.setVisible(true);
    }

    @Override
    public void workerUpdated(StreamerContentModel<T> model) {
        if (!isActive()) {
            setActive(false);
        }
        streamerViewHandler.getCurrentView().requestFocus();
        updateAddressBarAndStreamerPanel();
    }

    @Override
    public void workerFinished()
    {

    }

    private void updateAddressBarAndStreamerPanel() {
        ofNullable(getLoadedStreamer())
            .ifPresent(loadedStreamer -> ofNullable(loadedStreamer.getUrlPath())
                .map(address -> address.split("://"))
                .map(address -> address[address.length - 1])
                .ifPresent(address -> {
                    addressBar.setAddress(address);
                    getPanel().ifPresent(panel -> {
                        panel.setAddress(address);
                        ofNullable(loadedStreamer.getParent())
                            .ifPresent(parent -> panel.setFreeSpace(
                                parent.getFreeSpace(),
                                parent.getTotalSpace()));
                    });
                }));
    }

    @Override
    public void workerFinished(StreamerContentModel<T> streamedContent) {
        ofNullable(getLoadedStreamer())
            .filter(streamer -> PackStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(PackStreamer.class::cast)
            .ifPresent(PackStreamer::finishLoading);
        loadingIndicator.setVisible(false);
        streamerViewStorage.saveLastLoadedStreamer(getLoadedStreamer(), getCurrentLocation());

        ofNullable(streamedContent.getParentStreamer())
            .filter(streamer -> PackStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(PackStreamer.class::cast)
            .ifPresent(PackStreamer::finishLoading);
        workerFinished();
    }

    public Optional<StreamerPanel> getPanel() {
        return streamerViewHandler.getPanel(getCurrentLocation());
    }

    public boolean isActive() {
        return location == streamerViewHandler.getCurrentLocation();
    }

    public void showRename() {
    }

    public abstract String getName();

    public abstract void setActive(boolean active);

    public abstract void selectCurrentContent();

    public abstract void findContent(String name);

    public abstract T getCurrentContent();

    public abstract Streamer<T> getLoadedStreamer();

    public abstract Rectangle getCurrentRectangle();

    public abstract void goHome();

    public abstract void goEnd();

    public abstract List<T> getSelectedContent();

    public abstract Optional<String> getSelectedContentIdentifier();

    public abstract StreamerContentModel<T> getModel();
}
