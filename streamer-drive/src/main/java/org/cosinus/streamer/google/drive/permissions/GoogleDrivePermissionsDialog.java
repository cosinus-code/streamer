/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.google.drive.permissions;

import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.ComboBox;
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.layout.SpringGridLayout;
import org.cosinus.swing.text.HtmlText;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.window.Dialog;
import org.springframework.beans.factory.annotation.Autowired;


import java.awt.*;
import java.awt.datatransfer.StringSelection;

import static java.awt.BorderLayout.*;
import static java.awt.FlowLayout.RIGHT;
import static org.cosinus.streamer.google.drive.permissions.GoogleDrivePermissions.AVAILABLE_ROLES;
import static org.cosinus.streamer.google.drive.permissions.GoogleDriveRole.*;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.icon.IconSize.X32;

public class GoogleDrivePermissionsDialog extends Dialog<GoogleDrivePermissions> {

    @Autowired
    private Translator translator;

    @Autowired
    private IconHandler iconHandler;

    private final GoogleDrivePermissions permissions;

    public GoogleDrivePermissionsDialog(final Frame frame,
                                        final String title,
                                        final GoogleDrivePermissions permissions) {
        super(frame, title, true, false);
        this.permissions = permissions;
    }

    @Override
    public void initComponents() {
        getRootPane().setBorder(emptyBorder(10));
        setLayout(new BorderLayout(10, 10));

        Panel usersPanel = new Panel();

        SpringGridLayout layout = new SpringGridLayout(usersPanel,
            permissions.getUserPermissions().size(), 2,
            5, 5,
            10, 10);
        usersPanel.setLayout(layout);

        permissions.getUserPermissions()
            .forEach(user -> {
                Label userLabel = new Label(new HtmlText() {
                    @Override
                    public String getHtml() {
                        return htmlText(wrappedHtml(
                            boldText(user.getDisplayName() +
                                (user.isMe() ? " (" + translator.translate("google-drive-me") + ")" : "")),
                            user.getDescription()));
                    }
                }.getHtml());
                userLabel.setIconName(user.getIconName());
                iconHandler.findIconByName(user.getIconName(), X32, true)
                    .ifPresent(userLabel::setIcon);

                usersPanel.add(userLabel);

                Component userRole = user.getRole() != OWNER ?
                    createRoleComboBox(user) :
                    new Label(user.getRole().getName());
                usersPanel.add(userRole);
            });

        Panel buttonsPanel = new Panel(new FlowLayout(RIGHT, 5, 5));

        Button copyLinkButton = new Button(translator.translate("google-drive-copy-link"));
        Button okButton = new Button(translator.translate("ok"));

        buttonsPanel.add(copyLinkButton);
        buttonsPanel.add(okButton);

        add(usersPanel, NORTH);
        add(buttonsPanel, SOUTH);
        layout.pack();

        registerExitOnEscapeKey();
        registerAction(okButton, e -> dispose());
        getRootPane().setDefaultButton(okButton);
        copyLinkButton.addAction(() -> {
            StringSelection selection = new StringSelection(permissions.getLink());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        });
        pack();
    }

    private ComboBox<GoogleDriveRole> createRoleComboBox(GoogleDriveUserPermission user) {
        ComboBox<GoogleDriveRole> roleComboBox = new ComboBox<>(AVAILABLE_ROLES, user.getRole());
        roleComboBox.addActionListener(event -> user.setRole(roleComboBox.getControlValue()));
        return roleComboBox;
    }

    @Override
    protected GoogleDrivePermissions getDialogResponse() {
        return permissions;
    }
}
