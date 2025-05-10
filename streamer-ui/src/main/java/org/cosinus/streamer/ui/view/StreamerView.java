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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.api.worker.WorkerListener;
import org.cosinus.streamer.ui.action.ChangeViewAction;
import org.cosinus.streamer.ui.action.ChangeViewActionContext;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.menu.MenuItem;
import org.cosinus.swing.menu.PopupMenu;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.awt.BorderLayout.*;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.view.View.findByName;
import static org.cosinus.streamer.ui.view.text.TextStreamerView.DIRTY_TEXT_MARKER;
import static org.cosinus.swing.image.icon.IconSize.X16;

public abstract class StreamerView<T, V> extends Panel implements WorkerListener<LoadWorkerModel<T, V>, V> {

    private static final Logger LOG = LogManager.getLogger(StreamerView.class);

    @Autowired
    protected StreamerViewHandler streamerViewHandler;

    @Autowired
    protected StreamerViewStorage streamerViewStorage;

    @Autowired
    protected LoadActionExecutor loadActionExecutor;

    @Autowired
    protected ChangeViewAction changeViewAction;

    @Autowired
    protected IconHandler iconHandler;

    @Autowired
    protected AddressBar addressBar;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected DialogHandler dialogHandler;

    @Autowired
    protected Translator translator;

    protected final String id;

    protected final PanelLocation location;

    protected final JPanel streamerViewMainPanel;

    protected FindPanel findPanel;

    protected LoadingProgress loadingIndicator;

    protected Streamer<T> parentStreamer;

    private PopupMenu alternativeViewsPopup;

    public StreamerView(PanelLocation location) {
        this.id = UUID.randomUUID().toString();
        this.location = location;

        streamerViewMainPanel = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
    }

    public Streamer<T> getParentStreamer() {
        return parentStreamer;
    }

    public void setParentStreamer(Streamer<T> parentStreamer) {
        this.parentStreamer = parentStreamer;
    }

    public void reset(final Streamer<T> parentStreamer) {
        this.parentStreamer = parentStreamer;
        alternativeViewsPopup = new PopupMenu();
        streamerViewHandler.getAvailableViewNames(parentStreamer)
            .stream()
            .map(this::viewMenuItem)
            .forEach(alternativeViewsPopup::add);
        alternativeViewsPopup.translate();
    }

    private MenuItem viewMenuItem(String viewName) {
        MenuItem menuItem = new MenuItem(viewAction(viewName), viewKey(viewName));
        findByName(viewName)
            .map(View::getIconName)
            .flatMap(iconName -> iconHandler.findIconByName(iconName, X16))
            .ifPresent(menuItem::setIcon);
        return menuItem;
    }

    private String viewKey(String viewName) {
        return findByName(viewName)
            .map(View::getKey)
            .orElse(viewName);
    }

    private ActionListener viewAction(String viewName) {
        return event -> changeViewAction.run(new ChangeViewActionContext(viewName));
    }

    public PopupMenu getAlternativeViewsPopup() {
        return alternativeViewsPopup;
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

        findPanel = createFindTextPanel();
        if (findPanel != null) {
            findPanel.initComponents();
            streamerViewMainPanel.add(findPanel, NORTH);
        }

        this.loadingIndicator = createLoadingIndicator();
        add(streamerViewMainPanel, CENTER);
        add(loadingIndicator, SOUTH);

        updateForm();
    }


    public void showDetailEditors() {
    }

    public void loadStreamer(Streamer<T> streamer) {
        loadStreamer(streamer, getCurrentItemIdentifier());
    }

    public void loadStreamer(Streamer<T> streamer, String contentIdentifier) {
        loadActionExecutor.execute(new LoadActionModel(getCurrentLocation(), streamer, contentIdentifier));
    }

    public void reload() {
        loadStreamer(this.getParentStreamer());
    }

    public void reload(String contentIdentifier) {
        loadStreamer(this.getParentStreamer(), contentIdentifier);
    }

    public PanelLocation getCurrentLocation() {
        return location;
    }

    public LoadingProgress getLoadingIndicator() {
        return loadingIndicator;
    }

    @Override
    public void workerStarted(LoadWorkerModel<T, V> loadWorkerModel) {
        try {
            loadingIndicator.startLoading(loadWorkerModel.getTotalSizeToLoad());
        } catch (Exception ex) {
            LOG.debug("Failed to start loading indicator", ex);
        }
        requestFocus();
    }

    @Override
    public void workerUpdated(LoadWorkerModel<T, V> loadWorkerModel) {
        loadingIndicator.updateLoading(loadWorkerModel.getLoadedSize(), loadWorkerModel.getTotalSizeToLoad());
    }

    @Override
    public void workerFinished(LoadWorkerModel<T, V> loadWorkerModel) {
        loadingIndicator.finishLoading();
        updateAddressBarAndStreamerPanel();

        streamerViewStorage.saveLastLoadedStreamer(this.getParentStreamer(), getCurrentLocation());

        StreamerView<?, ?> currentView = streamerViewHandler.getCurrentView();
        if (currentView != null && !currentView.hasFocus()) {
            currentView.requestFocus();
        }
    }

    public void updateAddressBarAndStreamerPanel() {
        getStreamerAddress().ifPresent(address -> {
            addressBar.setAddress(address);
            getPanel().ifPresent(panel -> {
                panel.setAddress(address);
                ofNullable(this.getParentStreamer())
                    .map(parent -> parent.isParent() ? parent : parent.getParent())
                    .map(ParentStreamer.class::cast)
                    .ifPresent(parent -> panel.setFreeSpace(
                        parent.getFreeSpace(),
                        parent.getTotalSpace()));
            });
        });
    }

    public boolean isDirty() {
        return ofNullable(getParentStreamer())
            .map(Streamer::isDirty)
            .orElse(false);
    }

    protected Optional<String> getStreamerAddress() {
        return ofNullable(this.getParentStreamer())
            .map(Streamer::getUrlPath)
            .map(address -> address.split("://"))
            .map(address -> address.length > 1 ? address[address.length - 1] : "")
            .map(address -> isDirty() ? DIRTY_TEXT_MARKER + address : address);
    }

    public Optional<StreamerPanel> getPanel() {
        return streamerViewHandler.getPanel(getCurrentLocation());
    }

    public boolean isActive() {
        return location == streamerViewHandler.getCurrentLocation();
    }

    public void setActive(boolean active) {
        getPanel().ifPresent(panel -> {
            panel.setEnabled(active);
            if (active) {
                updateAddressBarAndStreamerPanel();
            }
        });
    }

    public Panel getFindPanel() {
        return findPanel;
    }

    public void goNext() {
    }

    public void addCurrentItemToSelectionAndGoNext() {
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

    public <V> WorkerListener<SaveWorkerModel<V>, V> getSaveListener() {
        return null;
    }

    protected FindPanel createFindTextPanel() {
        return null;
    }

    public abstract String getName();

    public abstract T getCurrentItem();

    public abstract List<T> getSelectedItems();

    public abstract String getCurrentItemIdentifier();

    public abstract String getNextItemIdentifier();

    public abstract LoadWorkerModel<T, V> getLoadWorkerModel();

    protected abstract Container getContainer();
}
