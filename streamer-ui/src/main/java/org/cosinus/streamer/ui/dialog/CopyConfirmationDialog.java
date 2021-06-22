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

import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.execute.copy.TransferType;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.window.Dialog;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Optional;

import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.border.Borders.emptyBorder;

/**
 * Dialog used for confirmation of copy action
 */
public class CopyConfirmationDialog extends Dialog<CopyActionModel> {

    @Autowired
    private Translator translator;

    @Autowired
    private DialogHandler dialogHandler;

    private JTextField txtCopyTo;

    private final JComboBox<?> cmbTransferType;

    private final CopyActionModel copyAction;

    private final String actionName;

    public CopyConfirmationDialog(CopyActionModel copyAction, Object[] transferTypes) {
        super(applicationFrame, applicationFrame.getTitle(), true, false);
        this.copyAction = copyAction;
        this.actionName = translator.translate(copyAction.getActionName());
        this.cmbTransferType = new JComboBox<>(transferTypes);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        JLabel copyToLabel = new JLabel(translator.translate("form_copy_files", getActionName()));
        JLabel filterLabel = new JLabel(translator.translate("form_copy_only"));
        JComboBox<Object> cmbFilter = new JComboBox<>();
        JLabel lblTransferType = new JLabel(translator.translate("form_copy_trasfer_type") + "  ");

        JButton okButton = new JButton(translator.translate("form_copy_ok"));
        JButton cancelButton = new JButton(translator.translate("form_copy_cancel"));
        JButton browseButton = new JButton(translator.translate("form_copy_tree"));

        txtCopyTo = new JTextField(copyAction.getTargetPath().toString());

        boolean showTransferType = copyAction.isSensitiveToTransferType() || copyAction.shouldPackStreamers();
        lblTransferType.setVisible(showTransferType);
        cmbTransferType.setVisible(showTransferType);
        cmbTransferType.setSelectedIndex(0);

        ActionListener actionListener = av -> {
            if (av.getSource() == okButton) dispose();
            else if (av.getSource() == cancelButton) cancel();
            else if (av.getSource() == browseButton) browse();
        };

        okButton.addActionListener(actionListener);
        cancelButton.addActionListener(actionListener);
        browseButton.addActionListener(actionListener);

        cmbFilter.setEditable(true);

        copyToLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        filterLabel.setVerticalAlignment(SwingConstants.BOTTOM);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 4, 2));
        buttonsPanel.add(okButton);
        buttonsPanel.add(browseButton);
        buttonsPanel.add(cancelButton);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        southPanel.add(buttonsPanel);

        JPanel panTransferType = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panTransferType.setBorder(null);
        panTransferType.add(lblTransferType);
        panTransferType.add(cmbTransferType);

        JPanel copyPanel = new JPanel(new GridLayout(4, 1, 3, 3));
        copyPanel.setBorder(emptyBorder(0, 0, 10, 0));
        copyPanel.add(copyToLabel);
        copyPanel.add(txtCopyTo);
        copyPanel.add(filterLabel);
        copyPanel.add(cmbFilter);

        JPanel panCenter = new JPanel(new BorderLayout());
        panCenter.add(panTransferType, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(emptyBorder(2, 10, 5, 10));
        mainPanel.add(copyPanel, BorderLayout.NORTH);
        mainPanel.add(panCenter, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        filterLabel.setEnabled(false);
        cmbFilter.setEnabled(false);

        getContentPane().add(mainPanel);
        mainPanel.getRootPane().setDefaultButton(okButton);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(new Dimension(363, showTransferType ? 240 : 213));

        setLocationRelativeTo(getParent());
    }

    public void browse() {
        dialogHandler.chooseFile(this, true, txtCopyTo);
    }

    @Override
    protected CopyActionModel getDialogResponse() {
        if (copyAction.isSensitiveToTransferType()) {
            Optional.ofNullable(cmbTransferType.getSelectedItem())
                .filter(type -> TransferType.class.isAssignableFrom(type.getClass()))
                .map(TransferType.class::cast)
                .ifPresent(copyAction::withTransferType);
        }
        if (copyAction.shouldPackStreamers()) {
            Optional.ofNullable(cmbTransferType.getSelectedItem())
                .map(Object::toString)
                .ifPresent(copyAction::withPackType);
        }

        return copyAction.toTargetPath(txtCopyTo.getText());
    }

    protected String getActionName() {
        return actionName;
    }
}