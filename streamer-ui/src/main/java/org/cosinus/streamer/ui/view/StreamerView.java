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

import lombok.Getter;
import lombok.Setter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.ui.action.ChangeViewAction;
import org.cosinus.streamer.ui.action.ChangeViewActionModel;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.menu.MenuHandler;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.image.icon.IconInitializer;
import org.cosinus.swing.menu.PopupMenu;
import org.cosinus.swing.menu.RadioButtonMenuItem;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.progress.CustomProgressBar;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.awt.BorderLayout.*;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.GoToParentStreamerAction.GO_TO_PARENT_ACTION;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.SHOW_STATUS;
import static org.cosinus.streamer.ui.view.View.findByName;
import static org.cosinus.streamer.ui.view.text.TextStreamerView.DIRTY_TEXT_MARKER;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.file.FileHandler.PROTOCOL_MARK;

public abstract class StreamerView<T> extends Panel {

    public static final String STATUS_ITEMS_COUNT_KEY = "status-items-count";

    public static final String STATUS_SELECTED_ITEMS_COUNT_KEY = "status-selected-items-count";

    @Autowired
    private Preferences preferences;

    @Autowired
    protected StreamerViewHandler streamerViewHandler;

    @Autowired
    protected StreamerViewStorage streamerViewStorage;

    @Autowired
    protected LoadActionExecutor loadActionExecutor;

    @Autowired
    protected ChangeViewAction changeViewAction;

    @Autowired
    protected AddressBar addressBar;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected DialogHandler dialogHandler;

    @Autowired
    protected Translator translator;

    @Autowired
    protected IconInitializer iconInitializer;

    @Autowired
    protected MenuHandler menuHandler;

    @Autowired
    protected ActionController actionController;

    @Autowired
    protected ApplicationUIHandler uiHandler;

    @Getter
    protected final String id;

    protected final PanelLocation location;

    protected Panel streamerViewMainPanel;

    protected FindPanel findPanel;

    @Getter
    protected CustomProgressBar loadingIndicator;

    @Setter
    @Getter
    protected Streamer<T> parentStreamer;

    @Getter
    protected PopupMenu alternativeViewsPopup;

    private Label statusBar;

    private boolean cancelActionPrevented;

    protected Viewer<T> viewer;

    public StreamerView(PanelLocation location) {
        this.id = UUID.randomUUID().toString();
        this.location = location;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        streamerViewMainPanel = new Panel(new BorderLayout());
        setLayout(new BorderLayout());

        viewer = createViewer();
        if (viewer != null) {
            viewer.setView(this);
            if (viewer instanceof FormComponent formComponent) {
                formComponent.initComponents();
            }
        }

        findPanel = createTextFinder();
        if (findPanel != null) {
            findPanel.initComponents();
            streamerViewMainPanel.add(findPanel, NORTH);
        }

        this.loadingIndicator = createLoadingIndicator();
        add(streamerViewMainPanel, CENTER);

        statusBar = new Label(" ");
        if (haveStatus() && preferences.booleanPreference(SHOW_STATUS)) {
            statusBar.setBorder(emptyBorder(0, 3, 0, 3));
            Panel streamerVieBottomPanel = new Panel(new BorderLayout());
            streamerVieBottomPanel.add(statusBar, NORTH);
            streamerVieBottomPanel.add(loadingIndicator, SOUTH);
            add(streamerVieBottomPanel, SOUTH);
        } else {
            add(loadingIndicator, SOUTH);
        }
    }

    protected boolean haveStatus() {
        return true;
    }

    public void reset(final Streamer<T> parentStreamer) {
        this.parentStreamer = parentStreamer;
        alternativeViewsPopup = new PopupMenu();
        streamerViewHandler.getAvailableViewNames(parentStreamer)
            .stream()
            .map(this::createViewMenuItem)
            .forEach(alternativeViewsPopup::add);
        alternativeViewsPopup.translate();
        if (viewer != null) {
            viewer.reset(parentStreamer);
        }
    }

    private RadioButtonMenuItem createViewMenuItem(String viewName) {
        RadioButtonMenuItem menuItem = new RadioButtonMenuItem(
            changeViewAction(viewName),
            getName().equals(viewName),
            viewKey(viewName));
        findByName(viewName)
            .map(View::getIconName)
            .ifPresent(menuItem::setIconName);
        iconInitializer.updateIcon(menuItem);
        return menuItem;
    }

    private String viewKey(String viewName) {
        return findByName(viewName)
            .map(View::getKey)
            .orElse(viewName);
    }

    private ActionListener changeViewAction(String viewName) {
        return event -> changeViewAction.run(new ChangeViewActionModel(viewName));
    }

    protected CustomProgressBar createLoadingIndicator() {
        CustomProgressBar loading = new CustomProgressBar();
        loading.setIndeterminate(true);
        loading.setPreferredSize(new Dimension(getWidth(), 7));
        return loading;
    }

    protected void initKeyActionsHandler() {
        getContainer().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                actionController.runActionByKeyStroke(e);
            }
        });
    }

    protected void initCancelHandler() {
        getContainer().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == VK_ESCAPE) {
                    if (!cancelActionPrevented) {
                        actionController.runAction(GO_TO_PARENT_ACTION);
                    }
                    cancelActionPrevented = false;
                }
            }
        });
    }

    protected void initFocusHandling() {
        getContainer().setFocusable(true);
        getContainer().setFocusCycleRoot(true);
        getContainer().setFocusTraversalKeysEnabled(false);
        getContainer().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                try {
                    streamerViewHandler.setCurrentLocation(getCurrentLocation());
                } catch (Exception ex) {
                    errorHandler.handleError(getContainer(), ex);
                }
            }
        });
        getContainer().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getContainer().requestFocus();
            }
        });
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        if (getContainer() != null) {
            getContainer().requestFocus();
        }
    }
    public void preventCancelAction() {
        cancelActionPrevented = true;
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

    public void updateStreamerViewIdentifiers() {
        getStreamerAddress().ifPresent(address -> {
            addressBar.setAddress(address);
            getPanel().ifPresent(panel -> {
                panel.setAddress(address);
                ofNullable(this.getParentStreamer())
                    .map(parent -> parent.isParent() ? parent : parent.getParent())
                    .filter(parent ->
                        ParentStreamer.class.isAssignableFrom(parent.getClass()))
                    .map(ParentStreamer.class::cast)
                    .ifPresent(parent -> panel.setFreeSpace(
                        parent.getFreeSpace(),
                        parent.getTotalSpace()));
            });
        });
    }

    public void setStatus(String status) {
        statusBar.setText(status);
    }

    public boolean isDirty() {
        return ofNullable(getParentStreamer())
            .map(Streamer::isDirty)
            .orElse(false);
    }

    protected Optional<String> getStreamerAddress() {
        return ofNullable(this.getParentStreamer())
            .map(Streamer::getUrlPath)
            .map(address -> address.split(PROTOCOL_MARK))
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
        if (viewer != null) {
            viewer.setActive(active);
        }
        getPanel().ifPresent(panel -> {
            panel.setEnabled(active);
            if (active) {
                updateStreamerViewIdentifiers();
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

    protected void saveStreamerViewSnapshot() {
        streamerViewStorage.saveLastLoadedStreamer(this.getParentStreamer(), getCurrentLocation());
    }

    public <V> WorkerListener<LoadWorkerModel<V>, V> getLoadWorkerListener() {
        return new StreamerViewLoadListener<>(this);
    }

    public <V> SaveWorkerModel<V> getSaveWorkerModel() {
        return null;
    }

    public WorkerModel<Streamer<T>> getDeleteWorkerModel() {
        return null;
    }

    public WorkerModel<T> getCopyWorkerModel() {
        return null;
    }

    public void fireContentChanged() {
        repaint();
    }

    public <V> WorkerListener<SaveWorkerModel<V>, V> getSaveListener() {
        return new WorkerListener<>() {
            @Override
            public void workerStarted(SaveWorkerModel<V> saveTextModel) {
                loadingIndicator.startLoading(saveTextModel.totalItemsToSave());
            }

            @Override
            public void workerUpdated(SaveWorkerModel<V> saveTextModel) {
                loadingIndicator.updateLoading(saveTextModel.getSavedItemsCount(), saveTextModel.totalItemsToSave());
            }

            @Override
            public void workerFinished(SaveWorkerModel<V> workerModel) {
                setDirty(false);
                loadingIndicator.finishLoading();
            }
        };
    }

    public void setDirty(boolean dirty) {
    }

    protected FindPanel createTextFinder() {
        return null;
    }

    public void updateStatus() {
        setStatus(getStatus());
    }

    public Streamer<T> getCurrentStreamer() {
        return ofNullable(getCurrentItem())
            .filter(item -> Streamer.class.isAssignableFrom(item.getClass()))
            .map(Streamer.class::cast)
            .orElse(null);
    }

    public Streamer<T> getCurrentStreamerOrParent() {
        return ofNullable(getCurrentStreamer())
            .or(() -> ofNullable(getParentStreamer()))
            .orElse(null);
    }

    public List<ViewItem> getAllViewItems() {
        return emptyList();
    }

    public String getCurrentItemIdentifier() {
        return ofNullable(parentStreamer)
            .map(Streamer::getName)
            .orElse(null);
    }

    protected Container getContainer() {
        return viewer instanceof Container container ? container : null;
    }

    public String getStatus() {
        int selectedItemsCount = getSelectedItems().size();
        return selectedItemsCount > 0 ?
            translator.translate(STATUS_SELECTED_ITEMS_COUNT_KEY, selectedItemsCount, getItemsCount()) :
            translator.translate(STATUS_ITEMS_COUNT_KEY, getItemsCount());
    }

    protected abstract Viewer<T> createViewer();

    protected abstract long getItemsCount();

    public abstract String getName();

    public abstract T getCurrentItem();

    public abstract List<T> getSelectedItems();

    public abstract <V> LoadWorkerModel<V> getLoadWorkerModel();
}
