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

import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.format.FormatHandler;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;

public class FreeSpace extends Label {

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Autowired
    private Translator translator;

    @Autowired
    private FormatHandler formatHandler;
    private long freeSpace;

    private long totalSpace;

    public FreeSpace() {
        setBorder(emptyBorder(2));
    }

    @Override
    public void paint(Graphics g) {
        if (totalSpace > 0) {
            ofNullable(uiHandler.getColor("MenuItem.selectionBackground"))
                .or(uiHandler::getInactiveBackgroundColor)
                .ifPresent(color -> {
                    int width = (int) (getWidth() * (totalSpace - freeSpace) / totalSpace);
                    Color initialColor = g.getColor();
                    g.setColor(color);
                    g.fillRect(0, 0, width, getHeight());
                    g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                    g.setColor(initialColor);
                });
        }

        super.paint(g);
    }

    public void setFreeSpace(long freeSpace, long totalSpace) {
        this.freeSpace = freeSpace;
        this.totalSpace = totalSpace;

        setText(totalSpace > 0 ?
            translator.translate("free_memory",
                formatHandler.formatMemorySize(freeSpace),
                formatHandler.formatMemorySize(totalSpace)) :
            " ");
    }
}
