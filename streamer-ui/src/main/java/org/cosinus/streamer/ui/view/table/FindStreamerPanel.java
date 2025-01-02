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
package org.cosinus.streamer.ui.view.table;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.search.SearchStreamer;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.FindPanel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.CheckBox;
import org.cosinus.swing.form.control.FindTextField;
import org.cosinus.swing.layout.SpringGridLayout;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

import static org.cosinus.swing.border.Borders.emptyBorder;

/**
 * Panel for controls to find streamers
 */
public class FindStreamerPanel<S extends Streamer<S>> extends FindPanel {

    @Autowired
    protected Translator translator;

    @Autowired
    protected LoadActionExecutor<SearchStreamer<S>> loadActionExecutor;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    private final StreamerView<?, ?> streamerView;

    private FindTextField streamerContentFindTextField;

    private CheckBox deepInsideCheckBox;

    private Button findButton;

    public FindStreamerPanel(StreamerView<?, ?> streamerView) {
        this.streamerView = streamerView;
    }

    @Override
    public void initComponents() {
        findTextField = new FindTextField(this::performFindAction);
        deepInsideCheckBox = new CheckBox(translator.translate("find-deep-search"), true);

        streamerContentFindTextField = new FindTextField(this::performFindAction);
        findButton = new Button(translator.translate("find-action"), this::performFindAction);

        findTextField.setActionOnSettingChange(false);
        streamerContentFindTextField.setActionOnSettingChange(false);

        Dimension dimension = new Dimension(130, 28);
        deepInsideCheckBox.setPreferredSize(dimension);
        findButton.setPreferredSize(dimension);

        deepInsideCheckBox.setMaximumSize(dimension);
        findButton.setMaximumSize(dimension);

        SpringGridLayout findLayout = new SpringGridLayout(this,
            2, 3,
            5, 0,
            5, 5);
        setLayout(findLayout);

        add(findTextField.createAssociatedLabel(translator.translate("find-name")));
        add(findTextField);
        add(deepInsideCheckBox);

        add(streamerContentFindTextField.createAssociatedLabel(translator.translate("find-content")));
        add(streamerContentFindTextField);
        add(findButton);

        findLayout.pack();

        setBorder(emptyBorder(5, 5, 5, 0));

        registerEscapeAction(this::hidePanel);
        setVisible(false);
    }

    @Override
    protected void performFindAction() {
        //TODO: to avoid cast
        ParentStreamer<S> parentStreamer = (ParentStreamer<S>) streamerView.getParentStreamer();
        if (parentStreamer != null) {
            SearchStreamer<S> searchStreamer = new SearchStreamer<S>(
                parentStreamer,
                findTextField.getControlValue(),
                streamerContentFindTextField.getControlValue(),
                deepInsideCheckBox.isSelected());

            final StreamerView<?, ?> currentView = streamerViewHandler.getCurrentView();
            LoadActionModel<SearchStreamer<S>> loadActionModel =
                new LoadActionModel<>(currentView.getCurrentLocation(), searchStreamer);
            loadActionExecutor.execute(loadActionModel);
        }
    }
}
