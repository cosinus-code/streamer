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
package org.cosinus.streamer.ui.view.text;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.DefaultWorkerListener;
import org.cosinus.streamer.ui.action.execute.WorkerListener;
import org.cosinus.streamer.ui.action.execute.save.SaveWorkerModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.error.ErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

import static java.awt.BorderLayout.CENTER;
import static java.util.Collections.singletonList;

public class TextStreamerView extends StreamerView<String> {
    public static final String TEXT_EDITOR = "text-editor";

    public static final String DIRTY_TEXT_MARKER = "*";

    @Autowired
    public ErrorHandler errorHandler;

    private final TextEditor textEditor;

    private WorkerListener<SaveTextModel> saveListener;

    public TextStreamerView(PanelLocation location) {
        super(location);
        this.textEditor = new TextEditor(this);
    }

    @Override
    public void initComponents() {
        super.initComponents();
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(textEditor);
        streamerViewMainPanel.add(scroll, CENTER);
        textEditor.initComponent();
        this.saveListener = new DefaultWorkerListener<>() {
            @Override
            public void workerStarted(SaveTextModel saveTextModel) {
                loadingIndicator.startLoading(saveTextModel.totalItemsToSave());
            }

            @Override
            public void workerUpdated(SaveTextModel saveTextModel) {
                loadingIndicator.updateLoading(saveTextModel.getSavedItemsCount(), saveTextModel.totalItemsToSave());
            }

            @Override
            public void workerFinished(SaveTextModel workerModel) {
                textEditor.setDirty(false);
                loadingIndicator.finishLoading();
            }
        };

    }

    @Override
    public String getName() {
        return TEXT_EDITOR;
    }

    @Override
    public String getCurrentContent()
    {
        return textEditor.getSelectedText();
    }

    @Override
    public List<String> getSelectedContent()
    {
        return singletonList(textEditor.getSelectedText());
    }

    @Override
    public Streamer<String> getLoadedStreamer()
    {
        return textEditor.getParentStreamer();
    }

    @Override
    public Optional<String> getSelectedContentIdentifier()
    {
        return Optional.empty();
    }

    @Override
    public LoadWorkerModel<String> getLoadWorkerModel()
    {
        return textEditor;
    }

    @Override
    public void workerStarted(LoadWorkerModel<String> loadWorkerModel)
    {
        super.workerStarted(loadWorkerModel);
        textEditor.setDirty(false);
        textEditor.setLoading(true);
    }

    @Override
    public void workerFinished(LoadWorkerModel<String> loadWorkerModel)
    {
        super.workerFinished(loadWorkerModel);
        textEditor.setCaretPosition(0);
        textEditor.requestFocus();
        textEditor.setDirty(false);
        textEditor.setLoading(false);
    }

    @Override
    protected Optional<String> getStreamerAddress()
    {
        return super.getStreamerAddress()
            .map(address -> textEditor.isDirty() ? DIRTY_TEXT_MARKER + address : address);
    }

    @Override
    public SaveWorkerModel<String> getSaveModel()
    {
        return textEditor.getSaveWorkerModel();
    }

    @Override
    public WorkerListener<SaveTextModel> getSaveWorkerListener()
    {
        return saveListener;
    }
}
