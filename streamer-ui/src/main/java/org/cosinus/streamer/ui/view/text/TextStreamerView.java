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
package org.cosinus.streamer.ui.view.text;

import lombok.Getter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.FindPanel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.form.ScrollPane;
import org.cosinus.swing.worker.WorkerListener;

import java.awt.*;
import java.util.List;

import static java.awt.BorderLayout.CENTER;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

/**
 * Text streamer view
 */
public class TextStreamerView extends StreamerView<String> {

    public static final String TEXT_EDITOR = "text-editor";

    public static final String DIRTY_TEXT_MARKER = "*";

    public static final String STATUS_TEXT_SIZE = "status-text-size";

    public static final String STATUS_DIRTY_TEXT_SIZE = "status-dirty-text-size";

    @Getter
    private final TextStreamerEditor textEditor;

    public TextStreamerView(PanelLocation location) {
        super(location);
        textEditor = new TextStreamerEditor(this);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        ScrollPane scroll = new ScrollPane();
        scroll.setViewportView(textEditor);
        ofNullable(textEditor.getBackground())
            .map(Color::getRGB)
            .map(Color::new)
            .ifPresent(scroll.getViewport()::setBackground);

        streamerViewMainPanel.add(scroll, CENTER);

        textEditor.initComponent();
    }

    @Override
    public String getName() {
        return TEXT_EDITOR;
    }

    @Override
    public String getCurrentItem() {
        return textEditor.getSelectedText();
    }

    @Override
    public List<String> getSelectedItems() {
        return singletonList(textEditor.getSelectedText());
    }

    @Override
    public String getCurrentItemIdentifier() {
        return ofNullable(parentStreamer)
            .map(Streamer::getName)
            .orElse(null);
    }

    @Override
    public String getNextItemIdentifier() {
        return null;
    }

    @Override
    public LoadWorkerModel<String> getLoadWorkerModel() {
        return textEditor;
    }

    @Override
    protected Container getContainer() {
        return textEditor;
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || ofNullable(textEditor)
            .map(TextStreamerEditor::isDirty)
            .orElse(false);
    }

    @Override
    public TextStreamerViewLoadWorkerListener getLoadWorkerListener() {
        return new TextStreamerViewLoadWorkerListener(this);
    }

    @Override
    public SaveWorkerModel<String> getSaveWorkerModel() {
        return textEditor.getSaveWorkerModel();
    }

    @Override
    public WorkerListener<SaveTextWorkerModel, String> getSaveListener() {
        return new WorkerListener<>() {
            @Override
            public void workerStarted(SaveTextWorkerModel saveTextModel) {
                loadingIndicator.startLoading(saveTextModel.totalItemsToSave());
            }

            @Override
            public void workerUpdated(SaveTextWorkerModel saveTextModel) {
                loadingIndicator.updateLoading(saveTextModel.getSavedItemsCount(), saveTextModel.totalItemsToSave());
            }

            @Override
            public void workerFinished(SaveTextWorkerModel workerModel) {
                textEditor.setDirty(false);
                loadingIndicator.finishLoading();
            }
        };
    }

    @Override
    public void reset(final Streamer<String> parentStreamer) {
        super.reset(parentStreamer);
        textEditor.reset();
    }

    @Override
    protected FindPanel createFindTextPanel() {
        return new FindTextPanel(textEditor);
    }

    @Override
    public String getStatus() {
        return translator.translate(
            textEditor.isDirty() ? STATUS_DIRTY_TEXT_SIZE : STATUS_TEXT_SIZE,
            textEditor.getText().length());
    }
}
