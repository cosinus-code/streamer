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

import org.cosinus.swing.form.Split;
import org.cosinus.swing.menu.MenuItem;
import org.cosinus.swing.menu.PopupMenu;
import org.cosinus.swing.listener.SimpleMouseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.stream.IntStream;

import static java.awt.event.MouseEvent.BUTTON3;

public class MainSplit extends Split implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(MainSplit.class);

    private static final String MAIN_SPLITTER_NAME = "main-splitter";

    private static final int DEFAULT_DIVIDER_LOCATION = 391;

    private PopupMenu popupSplitter;

    public MainSplit() {
        super(MAIN_SPLITTER_NAME,
              DEFAULT_DIVIDER_LOCATION);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() instanceof MenuItem) {
                String key = ((MenuItem) e.getSource()).getActionKey();
                String[] pieces = key.split("-");
                moveSplitter(Integer.parseInt(pieces[pieces.length - 1]));
            }
        } catch (Exception ex) {
            LOG.error("Cannot move splitter", ex);
        }
    }

    @Override
    public void initComponent() {
        super.initComponent();

        setBorder(null);
        if (divider != null) {
            divider.setBorder(BorderFactory.createMatteBorder(10, 2, 10, 1,
                                                              divider.getBackground()));
        }
        setDividerSize(3);

        popupSplitter = new PopupMenu();
        IntStream.range(2, 9)
            .map(i -> i * 10)
            .forEach(value -> popupSplitter.add(new MenuItem(this,
                                                             "popup-splitter-" + value)));
        divider.addMouseListener(new SimpleMouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == BUTTON3) {
                    popupSplitter.show(divider,
                                       e.getX(),
                                       e.getY());
                }
            }
        });
        setFocusable(false);
    }

    @Override
    public void translate() {
        if (popupSplitter != null) {
            popupSplitter.translate();
        }
    }
}
