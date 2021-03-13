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
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.form.Frame;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Optional;

/**
 * Dialog used for confirmation of copy action
 */
public class CopyConfirmationDialog extends Dialog<CopyActionModel> {

    @Autowired
    private Translator translator;

    @Autowired
    private DialogHandler dialogHandler;

    @Autowired
    private ApplicationProperties applicationProperties;

    private JTextField txtCopyTo;

    private final JComboBox<?> cmbTransferType;

    private final CopyActionModel copyAction;

    private final String actionName;

    public CopyConfirmationDialog(Frame parent,
                                  String title,
                                  CopyActionModel copyAction,
                                  Object[] transferTypes) {
        super(parent, title, true);
        this.copyAction = copyAction;
        this.actionName = translator.translate(copyAction.getActionName());
        this.cmbTransferType = new JComboBox<>(transferTypes);

        initComponents();
        setLocationRelativeTo(parent);
    }

    public void initComponents() {
        JPanel panMain = new JPanel();
        JPanel panSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JPanel panButtons = new JPanel(new GridLayout(1, 3, 4, 2));
        JPanel panInfo = new JPanel(new GridLayout(4, 1, 3, 3));

        JLabel lblCopyTo = new JLabel(translator.translate("form_copy_files", getActionName()));
        JLabel lblFilter = new JLabel(translator.translate("form_copy_only"));
        JComboBox cmbFilter = new JComboBox();
        JLabel lblTransferType = new JLabel(translator.translate("form_copy_trasfer_type") + "  ");

        JButton btnOK = new JButton(translator.translate("form_copy_ok"));
        JButton btnCancel = new JButton(translator.translate("form_copy_cancel"));
        JButton btnBrowse = new JButton(translator.translate("form_copy_tree"));

        txtCopyTo = new JTextField(copyAction.getTargetPath().toString());

        boolean showTransferType = copyAction.isSensitiveToTransferType() || copyAction.shouldPackElements();
        lblTransferType.setVisible(showTransferType);
        cmbTransferType.setVisible(showTransferType);
        cmbTransferType.setSelectedIndex(0);

        setSize(new Dimension(363, showTransferType ? 220 : 193));

        ActionListener actionListener = av -> {
            if (av.getSource() == btnOK) dispose();
            else if (av.getSource() == btnCancel) cancel();
            else if (av.getSource() == btnBrowse) browse();
        };

        panMain.setLayout(new BorderLayout());

        btnOK.addActionListener(actionListener);
        btnCancel.addActionListener(actionListener);
        btnBrowse.addActionListener(actionListener);

        cmbFilter.setEditable(true);

        lblCopyTo.setVerticalAlignment(SwingConstants.BOTTOM);
        lblFilter.setVerticalAlignment(SwingConstants.BOTTOM);

        panButtons.add(btnOK);
        panButtons.add(btnBrowse);
        panButtons.add(btnCancel);
        panSouth.add(panButtons);

        JPanel panTransferType = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panTransferType.setBorder(null);
        panTransferType.add(lblTransferType);
        panTransferType.add(cmbTransferType);

        panInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panInfo.add(lblCopyTo);
        panInfo.add(txtCopyTo);
        panInfo.add(lblFilter);
        panInfo.add(cmbFilter);

        cmbTransferType.setPreferredSize(new Dimension(70, 20));

        JPanel panCenter = new JPanel(new BorderLayout());
        panCenter.add(panTransferType, BorderLayout.CENTER);

        panMain.setBorder(BorderFactory.createEmptyBorder(2, 10, 5, 10));
        panMain.add(panSouth, BorderLayout.SOUTH);
        panMain.add(panCenter, BorderLayout.CENTER);
        panMain.add(panInfo, BorderLayout.NORTH);

        lblFilter.setEnabled(false);
        cmbFilter.setEnabled(false);

        getContentPane().add(panMain);
        panMain.getRootPane().setDefaultButton(btnOK);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
        if (copyAction.shouldPackElements()) {
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