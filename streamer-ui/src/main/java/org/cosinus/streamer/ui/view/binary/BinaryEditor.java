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
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.form.Panel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.ArrayUtils.toObject;

public class BinaryEditor extends Panel implements LoadWorkerModel<byte[]> {

    private final BinaryStreamerView view;

    private final int endLineAddition = lineSeparator().length() == 2 ? 1 : 0;

    @Getter
    private final List<Byte> bytes;

    @Getter
    private BinaryHexaView binaryHexaView;

    @Getter
    private BinaryTextView binaryTextView;

    @Getter
    private int lineSize;

    @Getter
    private int currentByteIndex;

    private final AtomicBoolean updating = new AtomicBoolean();

    public BinaryEditor(final BinaryStreamerView view) {
        this.view = view;
        this.bytes = new ArrayList<>();
    }

    @Override
    public void initComponents() {
        binaryHexaView = new BinaryHexaView(view);
        binaryTextView = new BinaryTextView(view);
        binaryHexaView.initComponents();
        binaryTextView.initComponents();

        setLayout(new BorderLayout(1, 1));
        add(binaryHexaView, CENTER);
        add(binaryTextView, EAST);

        binaryHexaView.addCaretListener(e -> updateView(() -> {
            int binaryPosition = e.getDot();
            int textPosition = binaryPositionToTextPosition(binaryPosition);
            binaryTextView.setCaretPosition(textPosition);
        }));

        binaryTextView.addCaretListener(e -> updateView(() -> {
            int textPosition = e.getDot();
            int binaryPosition = textPositionToBinaryPosition(textPosition);
            binaryHexaView.setCaretPosition(binaryPosition);
        }));
    }

    public int binaryPositionToTextPosition(int binaryPosition) {
        int lineCount = binaryPosition / (lineSize * 3);
        int addition = lineCount * endLineAddition;
        currentByteIndex = (binaryPosition - addition) / 3;
        System.out.println("*** currentByteIndex: " + currentByteIndex);
        return currentByteIndex + lineCount + addition;
    }

    public int textPositionToBinaryPosition(int textPosition) {
        int lineCount = textPosition / (lineSize + lineSeparator().length());
        int addition = lineCount * endLineAddition;
        currentByteIndex = textPosition - addition - lineCount;
        return currentByteIndex + (textPosition - addition - lineCount) * 2;
    }

    public int byteIndexToBinaryPosition(int byteIndex) {
        int lineCount = lineSize > 0 ? byteIndex / lineSize : 0;
        return 3 * byteIndex + lineCount * endLineAddition;
    }

    public int binaryPositionToByteIndex(int binaryPosition) {
        int lineCount = binaryPosition / (lineSize * 3);
        int addition = lineCount * endLineAddition;
        return (binaryPosition - addition) / 3;
    }

    public void onResize(int width, int height) {
        lineSize = width / (3 * binaryHexaView.getCharWidth() + binaryTextView.getCharWidth() + 1);
        binaryHexaView.setText("");
        binaryTextView.setText("");
        refreshViews();
    }

    public void refreshViews() {
        binaryHexaView.reset();
        binaryTextView.reset();
        binaryHexaView.sync(bytes);
        binaryTextView.sync(bytes);
    }

    @Override
    public void update(List<byte[]> items) {
        items.stream()
            .flatMap(bytes -> stream(toObject(bytes)))
            .forEach(bytes::add);
    }

    public void updateCurrentByte(int byteIndex, Byte byteValue) {
        bytes.set(byteIndex, byteValue);
        updateView(() -> binaryTextView.updateCharAtPosition(byteIndex, byteValue));
    }

    private void updateView(Runnable runnable) {
        if (!updating.get()) {
            updating.set(true);
            try {
                runnable.run();
            } finally {
                updating.set(false);
            }
        }
    }
}
