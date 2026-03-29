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

package org.cosinus.streamer.ui.view.binary;

import lombok.Getter;
import org.cosinus.swing.form.BlockCaret;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

import static java.lang.System.lineSeparator;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.color.Colors.getLighterColor;
import static org.cosinus.swing.color.SystemColor.LABEL_BACKGROUND;

public class BinaryTextView extends BinaryView {

    @Getter
    private int charWidth;

    private final BinaryStreamerView view;

    public BinaryTextView(final BinaryStreamerView view) {
        this.view = view;
    }

    @Override
    public void initComponents() {
        setFont(uiHandler.getDefaultMonospacedFont().deriveFont(13f));
        setCaret(new BlockCaret(this));
        setBorder(emptyBorder(1));
        setBackground(getLighterColor(uiHandler.getColor(LABEL_BACKGROUND)));
        charWidth = getFontMetrics(getFont()).charWidth(' ');

        setNavigationFilter(new BinaryNavigationFilter(this));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                try {
                    streamerViewHandler.setCurrentLocation(view.getCurrentLocation());
                } catch (Exception ex) {
                    errorHandler.handleError(view, ex);
                }
            }
        });
    }

    @Override
    protected boolean isKeyAllowed(KeyEvent keyEvent) {
        return isEditorKey(keyEvent);
    }

    @Override
    public boolean isInvalidCharAtPosition(int position) {
        return getCharAtPosition(position)
            .map(character -> character == '\n' || character == '\r')
            .orElse(false);
    }

    @Override
    protected String getByteRepresentation(Byte byteValue) {
        long count = bytesCounter.getAndIncrement();
        int lineSize = view.getBinaryEditor().getLineSize();
        String textSeparator = count > 1 && count % lineSize == 0 ? lineSeparator() : "";
        return textSeparator + byteToChar(byteValue);
    }

    protected char byteToChar(Byte byteValue) {
        char character = (char) (byteValue & 0xFF);
        return (isLetter(character) ? character : '.');
    }

    public int byteIndexToPosition(int byteIndex) {
        int lineSize = view.getBinaryEditor().getLineSize();
        int lineCount = lineSize > 0 ? byteIndex / lineSize : 0;
        return byteIndex + lineCount * lineSeparator().length();
    }

    public void updateCharAtPosition(int byteIndex, Byte byteValue) {
        char character = byteToChar(byteValue);
        int position = byteIndexToPosition(byteIndex);
        if (position + 1 < getDocument().getLength()) {
            replaceRange("" + character, position, position + 1);
        }
    }
}
