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
package org.cosinus.streamer.ui.dialog;

import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.cosinus.streamer.pack.archive.ArchiveExpander;
import org.cosinus.streamer.ui.action.execute.pack.PackActionModel;
import org.cosinus.swing.ui.UIStructure;
import org.cosinus.swing.ui.UiInitializer;
import org.cosinus.swing.window.Dialog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

public class PackConfirmationDialog extends Dialog<PackActionModel> {

    private static final String UI_DESCRIPTOR_NAME = "packConfirmationDialog.json";

    protected static final String PACK_TYPE = "packType";

    protected static final String PACK_TO = "packTo";

    protected static final String PACK_FILTER = "packFilter";

    @Autowired
    private UiInitializer uiInitializer;

    @Autowired
    private BinaryExpanderHandler expanderHandler;

    protected final PackActionModel packAction;

    protected UIStructure uiStructure;

    public PackConfirmationDialog(PackActionModel packAction) {
        super(applicationFrame, applicationFrame.getTitle(), true, false);
        this.packAction = packAction;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        uiStructure = uiInitializer.createUiStructure(UI_DESCRIPTOR_NAME);
        getContentPane().add(uiStructure);
        uiStructure.getDefaultButton()
            .ifPresent(uiStructure.getRootPane()::setDefaultButton);
        registerDefaultActions(uiStructure);

        uiStructure.findControl(PACK_FILTER)
            .ifPresent(control -> control.setControlEnabled(false));
        uiStructure.getFileControl(PACK_TO)
            .setControlValue(packAction.getTargetPath().toFile());
        uiStructure.getComboBoxControl(PACK_TYPE)
            .setValues(expanderHandler.getBinaryExpandersMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof ArchiveExpander)
                .map(Map.Entry::getKey)
                .toArray(String[]::new));

        pack();
        centerWindow();
    }

    @Override
    protected PackActionModel getDialogResponse() {
        return packAction.withPackType(uiStructure.getStringValue(PACK_TYPE));
    }
}
