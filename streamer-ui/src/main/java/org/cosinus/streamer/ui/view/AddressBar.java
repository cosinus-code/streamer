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

import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.control.SimpleButton;
import org.cosinus.swing.form.control.TextField;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.image.icon.IconInitializer;
import org.cosinus.swing.layout.SpringGridLayout;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import java.awt.*;

import static org.cosinus.streamer.ui.action.FindAndLoadStreamerAction.findAndLoadStreamer;
import static org.cosinus.streamer.ui.action.GoToParentStreamerAction.GO_TO_PARENT_ACTION;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.image.icon.IconProvider.*;

@Component
public class AddressBar extends Panel {

    private final IconHandler iconHandler;

    private final ApplicationUIHandler uiHandler;

    private final ActionController actionController;

    private final IconInitializer iconInitializer;

    private TextField addressField;

    public AddressBar(final IconHandler iconHandler,
                      final ApplicationUIHandler uiHandler,
                      final ActionController actionController,
                      final IconInitializer iconInitializer) {
        this.iconHandler = iconHandler;
        this.uiHandler = uiHandler;
        this.actionController = actionController;
        this.iconInitializer = iconInitializer;
    }

    @Override
    public void initComponents() {
        addressField = new TextField();

        SimpleButton backButton = new SimpleButton(ICON_BACK, "<");
        SimpleButton nextButton = new SimpleButton(ICON_NEXT, ">");
        SimpleButton upButton = new SimpleButton(ICON_UP, "Λ");

        iconInitializer.updateIcon(backButton, nextButton, upButton);

        Dimension addressDimension = new Dimension(32, 32);
        backButton.setPreferredSize(addressDimension);
        nextButton.setPreferredSize(addressDimension);
        upButton.setPreferredSize(addressDimension);

        upButton.addAction(() -> actionController.runAction(GO_TO_PARENT_ACTION));
        addressField.addAction(findAndLoadStreamer(addressField::getText));

        backButton.setFocusable(false);
        nextButton.setFocusable(false);
        upButton.setFocusable(false);

        SpringGridLayout layout = new SpringGridLayout(this,
            1, 4,
            1, 3,
            1, 3);

        setOpaque(true);
        setBackground(uiHandler.getControlColor());

        Panel addressPanel = new Panel(new BorderLayout());
        addressPanel.setPreferredSize(addressDimension);
        addressPanel.setBorder(emptyBorder(2, 1, 2, 3));
        addressPanel.add(addressField);

        setLayout(layout);
        add(backButton);
        add(nextButton);
        add(upButton);
        add(addressPanel);
        layout.pack();
    }

    public void setAddress(String address) {
        addressField.setText(address);
    }
}
