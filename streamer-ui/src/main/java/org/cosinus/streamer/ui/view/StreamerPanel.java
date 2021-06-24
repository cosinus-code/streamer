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
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;

import static java.awt.BorderLayout.*;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ADDRESS_TOP;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.util.Formatter.formatTextForLabel;

public class StreamerPanel extends Panel {

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Autowired
    private Preferences preferences;

    @Autowired
    private Translator translator;

    private final Label addressLabel;

    private final JLabel freeSpaceLabel;

    private final FreeSpace freeSpace;

    private StreamerView view;

    public StreamerPanel() {
        this.addressLabel = new Label(" ");
        this.freeSpace = new FreeSpace();
        this.freeSpaceLabel = new JLabel();

        setLayout(new BorderLayout());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        addressLabel.setBorder(emptyBorder(2));
        addressLabel.setOpaque(true);
        uiHandler.getInactiveBackgroundColor()
            .ifPresent(addressLabel::setBackground);
        uiHandler.getInactiveForegroundColor()
            .ifPresent(addressLabel::setForeground);

        freeSpaceLabel.setText(translator.translate("free_space"));
        freeSpaceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        freeSpaceLabel.setVisible(false);

        JPanel freesSpacePanel = new JPanel(new BorderLayout(5, 5));
        freesSpacePanel.add(freeSpaceLabel, CENTER);
        freesSpacePanel.add(freeSpace, EAST);
        freesSpacePanel.setBorder(emptyBorder(3));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(freesSpacePanel, NORTH);
        if (preferences.booleanPreference(ADDRESS_TOP)) {
            topPanel.add(addressLabel, SOUTH);
        }

        add(topPanel, NORTH);
    }

    public void setView(StreamerView view) {
        view.initComponents();

        ofNullable(this.view)
            .ifPresent(this::remove);
        add(view, CENTER);
        revalidate();

        this.view = view;
    }

    public void setFreeSpace(long freeSpace, long totalSpace) {
        freeSpaceLabel.setVisible(totalSpace > 0);
        this.freeSpace.setFreeSpace(freeSpace, totalSpace);
    }

    public void setAddress(String address) {
        addressLabel.setText(formatTextForLabel(addressLabel, address));
    }

    public StreamerView getView() {
        return view;
    }

    @Override
    public void translate() {
        view.translate();
    }
}
