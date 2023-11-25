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
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.ui.action.execute.WorkerListener;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.action.execute.save.SaveWorkerModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyInsets;

public abstract class StreamerView<T> extends Panel implements WorkerListener<LoadWorkerModel<T>> {

    @Autowired
    protected StreamerViewHandler streamerViewHandler;

    @Autowired
    protected StreamerHandler streamerHandler;

    @Autowired
    public StreamerViewStorage streamerViewStorage;

    @Autowired
    public LoadActionExecutor loadActionExecutor;

    @Autowired
    public AddressBar addressBar;

    @Autowired
    public ErrorHandler errorHandler;

    @Autowired
    private DialogHandler dialogHandler;

    @Autowired
    private Translator translator;

    protected final String id;

    protected final PanelLocation location;

    protected final JPanel streamerViewMainPanel;

    protected LoadingProgress loadingIndicator;

    protected JTextComponent txtRename;

    private Streamer<?> streamerToBeRenamed;

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

    public Streamer<T> getParentStreamer() {
        return parentStreamer;
    }

    public void updateForm() {
        if (txtRename != null) {
            txtRename.setMargin(emptyInsets());
        }
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

        txtRename = getRenameComponent();
        txtRename.setVisible(false);
        txtRename.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                txtRename.setVisible(false);
            }
        });

        txtRename.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ENTER:
                            renameStreamer();
                        case KeyEvent.VK_ESCAPE:
                            hideControl();
                    }
                } catch (Exception ex) {
                    errorHandler.handleError(StreamerView.this, ex);
                }
            }
        });

        this.loadingIndicator = createLoadingIndicator();
        add(streamerViewMainPanel, CENTER);
        add(loadingIndicator, SOUTH);

        updateForm();
    }

    protected void validateInContainer(Container container) {
        container.add(txtRename);
        txtRename.validate();
    }

    private void hideControl() {
        txtRename.setVisible(false);
        SwingUtilities.invokeLater(StreamerView.this::requestFocus);
    }

    private void renameStreamer() {
        String newName = getRenameText();
        streamerToBeRenamed.details().put(new TranslatableName("name", null), new TextValue(newName));
        streamerToBeRenamed.save();
        reload(newName);
    }

    public void showRename() {
        if (txtRename == null) return;

        //TODO:
        streamerToBeRenamed = (Streamer<?>) getCurrentItem();
        if (streamerToBeRenamed == null) return;
        if (streamerToBeRenamed.equals(this.getParentStreamer())) return;

        if (!streamerToBeRenamed.canUpdate()) {
            dialogHandler.showInfo(translator.translate("rename.info.no.rename"));
        }

        txtRename.setText(streamerToBeRenamed.getName());
        txtRename.setBounds(getCurrentRectangle());
        txtRename.setVisible(true);
        txtRename.requestFocus();
        txtRename.selectAll();
    }

    protected String getRenameText() {
        return txtRename.getText();
    }

    protected JTextComponent getRenameComponent() {
        return new JTextField();
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
        ofNullable(this.getParentStreamer())
            .filter(streamer -> PackStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(PackStreamer.class::cast)
            .ifPresent(PackStreamer::finishLoading);
        loadingIndicator.finishLoading();

        streamerViewStorage.saveLastLoadedStreamer(this.getParentStreamer(), getCurrentLocation());

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
                ofNullable(this.getParentStreamer())
                    .map(Streamer::getParent)
                    .ifPresent(parent -> panel.setFreeSpace(
                        parent.getFreeSpace(),
                        parent.getTotalSpace()));
            });
        });
    }

    protected Optional<String> getStreamerAddress() {
        return ofNullable(this.getParentStreamer())
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

    public abstract T getCurrentItem();

    public abstract List<T> getSelectedItems();

    public abstract String getCurrentItemIdentifier();

    public abstract String getNextItemIdentifier();

    public abstract LoadWorkerModel<T> getLoadWorkerModel();

    public abstract Rectangle getCurrentRectangle();
}
