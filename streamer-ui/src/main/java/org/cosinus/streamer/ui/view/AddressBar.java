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

import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.TextField;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.layout.SpringGridLayout;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

import static org.cosinus.swing.image.icon.IconProvider.ICON_BACK;
import static org.cosinus.swing.image.icon.IconProvider.ICON_NEXT;
import static org.cosinus.swing.image.icon.IconSize.X16;

@Component
public class AddressBar extends Panel {

    private final IconHandler iconHandler;

    private TextField addressField;

    public AddressBar(IconHandler iconHandler) {
        this.iconHandler = iconHandler;
    }

    @Override
    public void initComponents() {
        addressField = new TextField();

        Button backButton = iconHandler.findIconByName(ICON_BACK, X16)
            .map(Button::new)
            .orElseGet(() -> new Button("<"));
        Button nextButton = iconHandler.findIconByName(ICON_NEXT, X16)
            .map(Button::new)
            .orElseGet(() -> new Button(">"));

        backButton.setFocusable(false);
        nextButton.setFocusable(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonsPanel.add(backButton);
        buttonsPanel.add(nextButton);

        SpringGridLayout layout = new SpringGridLayout(this,
                                                       1, 3,
                                                       1, 3,
                                                       1, 3);
        setLayout(layout);
        add(backButton);
        add(nextButton);
        add(addressField);
        layout.pack();
    }

    public void setAddress(String address) {
        addressField.setText(address);
    }
}
