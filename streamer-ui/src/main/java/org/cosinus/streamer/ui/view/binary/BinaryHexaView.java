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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.event.*;
import java.util.HexFormat;

import static java.lang.Character.toUpperCase;
import static java.lang.System.lineSeparator;
import static java.util.function.Predicate.not;
import static javax.swing.KeyStroke.getKeyStroke;

public class BinaryHexaView extends BinaryView {

    public static final String HEXA_FORMAT = "%02x";

    @Getter
    private int charWidth;

    private final BinaryStreamerView view;

    private BinaryDocumentFilter documentFilter;

    public BinaryHexaView(final BinaryStreamerView view) {
        this.view = view;
    }

    @Override
    public void initComponents() {
        setFont(uiHandler.getDefaultMonospacedFont().deriveFont(13f));
        setCaret(new BlockCaret(this));
        charWidth = getFontMetrics(getFont()).charWidth(' ');

        setFocusable(true);
        setFocusCycleRoot(true);
        setFocusTraversalKeysEnabled(false);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(getKeyStroke(KeyEvent.VK_ENTER, 0), "no-action");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocus();
            }
        });

        setNavigationFilter(new BinaryNavigationFilter(this));
        documentFilter = new BinaryDocumentFilter();
        if (getDocument() instanceof AbstractDocument document) {
            document.setDocumentFilter(documentFilter);
        }

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

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
            }
        });

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int byteIndex = view.getBinaryEditor().binaryPositionToByteIndex(e.getOffset());
                getCharAtPosition(e.getOffset())
                    .filter(not(Character::isWhitespace))
                    .flatMap(currentValue -> getCharAtPosition(e.getOffset() + 1)
                        .filter(not(Character::isWhitespace))
                        .map(endValue -> currentValue + "" + endValue)
                        .or(() -> getCharAtPosition(e.getOffset() - 1)
                            .filter(not(Character::isWhitespace))
                            .map(startValue -> startValue + "" + currentValue)))
                    .map(hexaRepresentation -> HexFormat.of().parseHex(hexaRepresentation))
                    .map(bytes -> bytes[0])
                    .ifPresent(byteValue -> view.getBinaryEditor().updateCurrentByte(byteIndex, byteValue));

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    @Override
    protected void processKeyEvent(KeyEvent keyEvent) {
        if (isHexaChar(keyEvent.getKeyChar())) {
            keyEvent.setKeyChar(toUpperCase(keyEvent.getKeyChar()));
        }
        super.processKeyEvent(keyEvent);
    }

    protected boolean isKeyAllowed(KeyEvent keyEvent) {
        return isMovementKey(keyEvent) || isHexaChar(keyEvent.getKeyChar()) || isDeleteKey(keyEvent);
    }

    @Override
    public boolean isInvalidCharAtPosition(int position) {
        return getCharAtPosition(position)
            .map(Character::isWhitespace)
            .orElse(false);
    }

    @Override
    public boolean isNavigationAllowed() {
        return !documentFilter.isSkipNavigation();
    }

    @Override
    protected String getByteRepresentation(Byte byteValue) {
        long count = bytesCounter.getAndIncrement();
        int lineSize = view.getBinaryEditor().getLineSize();
        String binarySeparator = count > 0 ? count % lineSize == 0 ? lineSeparator() : " " : "";
        return binarySeparator + HEXA_FORMAT.formatted(byteValue).toUpperCase();
    }
}
