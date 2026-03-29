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

import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.TextArea;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.awt.event.KeyEvent.KEY_PRESSED;

public abstract class BinaryView extends TextArea {

    @Autowired
    protected ApplicationUIHandler uiHandler;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected StreamerViewHandler streamerViewHandler;

    @Autowired
    protected ActionController actionController;

    protected final AtomicInteger bytesCounter = new AtomicInteger();

    @Override
    protected void processKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getID() == KEY_PRESSED) {
            actionController.runActionByKeyStroke(keyEvent);
        }

        if (isKeyAllowed(keyEvent)) {
            super.processKeyEvent(keyEvent);
        }
    }

    protected boolean isKeyAllowed(KeyEvent keyEvent) {
        return true;
    }

    public boolean isNavigationAllowed() {
        return true;
    }

    public boolean isInvalidCharAtPosition(int position) {
        return false;
    }

    protected void reset() {
        bytesCounter.set(0);
    }

    protected void sync(List<Byte> bytes) {
        String text = bytes
            .stream()
            .skip(bytesCounter.get())
            .map(this::getByteRepresentation)
            .collect(Collectors.joining());
        append(text);
    }

    protected abstract String getByteRepresentation(Byte byteValue);
}
