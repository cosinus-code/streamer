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
import org.cosinus.swing.form.ProgressBar;
import org.cosinus.swing.form.control.TextField;
import org.cosinus.swing.format.FormatHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;

import static java.awt.BorderLayout.*;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.FindAndLoadStreamerAction.findAndLoadStreamer;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ADDRESS_TOP;
import static org.cosinus.swing.border.Borders.emptyBorder;

public class StreamerPanel extends Panel {

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Autowired
    private Preferences preferences;

    @Autowired
    private Translator translator;

    @Autowired
    private FormatHandler formatHandler;

    private final TextField addressTop;

    private final JLabel freeSpaceLabel;

    private final ProgressBar freeSpaceMarker;

    private StreamerView<?, ?> view;

    public StreamerPanel() {
        this.addressTop = new TextField();
        this.freeSpaceMarker = new ProgressBar();
        this.freeSpaceLabel = new JLabel();

        setLayout(new BorderLayout());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        addressTop.setUI(new BasicTextFieldUI());
        addressTop.setFont(uiHandler.getLabelFont());
        addressTop.setBorder(emptyBorder(3, 5, 3, 5));
        addressTop.setOpaque(false);
        addressTop.addAction(findAndLoadStreamer(addressTop::getText));

        freeSpaceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        uiHandler.getInactiveForegroundColor()
            .ifPresent(freeSpaceLabel::setForeground);
        freeSpaceLabel.setVisible(false);

        JPanel freesSpacePanel = new JPanel(new BorderLayout(5, 5));
        freesSpacePanel.add(freeSpaceLabel, CENTER);
        freesSpacePanel.add(freeSpaceMarker, EAST);
        freesSpacePanel.setBorder(emptyBorder(3));
        freesSpacePanel.setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(freesSpacePanel, NORTH);
        if (preferences.booleanPreference(ADDRESS_TOP)) {
            topPanel.add(addressTop, SOUTH);
        }

        add(topPanel, NORTH);
    }

    public void setView(StreamerView<?, ?> view) {
        if (this.view == view) {
            return;
        }

        ofNullable(this.view)
            .ifPresent(this::remove);
        add(view, CENTER);

        this.view = view;
        this.view.initComponents();

        revalidate();
    }

    public void setFreeSpace(long freeSpace, long totalSpace) {
        boolean isFreeSpaceRelevant = totalSpace > 0;
        freeSpaceMarker.setValue(isFreeSpaceRelevant ? (int) (100 - freeSpace * 100 / totalSpace) : 0);
        freeSpaceLabel.setText(isFreeSpaceRelevant ?
            translator.translate("free_memory",
                formatHandler.formatMemorySize(freeSpace),
                formatHandler.formatMemorySize(totalSpace)) : "");
        freeSpaceLabel.setVisible(isFreeSpaceRelevant);
        freeSpaceMarker.setVisible(isFreeSpaceRelevant);
    }

    public void setAddress(String address) {
        addressTop.setText(formatHandler.formatTextForLabel(addressTop, address));
    }

    public StreamerView<?, ?> getView() {
        return view;
    }

    @Override
    public void translate() {
        ofNullable(view).ifPresent(StreamerView::translate);
    }

    @Override
    public void setEnabled(boolean enabled) {
        freeSpaceMarker.setEnabled(enabled);
        super.setEnabled(enabled);
    }
}
