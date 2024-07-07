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
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static org.apache.commons.compress.archivers.ArchiveStreamFactory.ZIP;

/**
 * Dialog used for confirmation of copy action
 */
public class PackConfirmationDialog extends CopyConfirmationDialog<PackActionModel> {

    @Autowired
    private BinaryExpanderHandler expanderHandler;

    private JComboBox<String> cmbTransferType;

    public PackConfirmationDialog(PackActionModel copyAction) {
        super(copyAction);
    }

    @Override
    public void createUiStructure() {
        super.createUiStructure();

        cmbTransferType = new JComboBox<>(expanderHandler.getBinaryExpandersMap()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() instanceof ArchiveExpander)
            .map(Map.Entry::getKey)
            .toArray(String[]::new));

        JLabel lblTransferType = new JLabel(translator.translate("form_copy_transfer_type"));
        cmbTransferType.setSelectedItem(ZIP);

        JPanel panTransferType = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panTransferType.setBorder(null);
        panTransferType.add(lblTransferType);
        panTransferType.add(cmbTransferType);

        JPanel panCenter = new JPanel(new BorderLayout());
        panCenter.add(panTransferType, BorderLayout.CENTER);

        mainPanel.add(panCenter, BorderLayout.CENTER);
    }

    @Override
    protected PackActionModel getDialogResponse() {
        copyAction.toTargetPath(txtCopyTo.getText());
        copyAction.withPackType(cmbTransferType.getSelectedItem().toString());
        return copyAction;
    }
}
