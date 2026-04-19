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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.SwingComponent;
import org.cosinus.swing.form.TextComponent;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.Adjustable.VERTICAL;
import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.Color.gray;
import static java.awt.Color.lightGray;
import static java.awt.event.KeyEvent.*;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.cosinus.swing.action.ActionController.NO_ACTION_ID;
import static org.cosinus.swing.color.Colors.inverseColor;
import static org.cosinus.swing.color.SystemColor.*;
import static org.cosinus.swing.image.ImageSettings.QUALITY;
import static org.cosinus.swing.math.MoreMath.fitInRange;

@Slf4j
public class BinaryHexaEditor extends SwingComponent implements LoadWorkerModel<byte[]>, TextComponent {

    public static final String OFFSET_FORMAT = "%08X";
    public static final String HEXA_FORMAT = "%02x";

    protected static final Stroke DASHED_STROKE = new BasicStroke(
        1, CAP_BUTT, JOIN_MITER, 10, new float[]{1, 3}, 0);
    private int bytesPerLine = 16;

    @Autowired
    protected ApplicationUIHandler uiHandler;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected StreamerViewHandler streamerViewHandler;

    @Autowired
    protected ActionController actionController;

    @Autowired
    protected LoadActionExecutor loadActionExecutor;

    private final BinaryStreamerView view;

    private final Map<Long, Byte> editedBytes;

    private final int padding = 5;
    private final int offsetPadding = 4;

    private long offset;
    private long totalSize;

    private long offsetLineIndex;
    private long maxLineIndex;

    private int linesCountPerPage;
    private int pageSize;

    private int textWidth;

    private int hexaViewX;
    private int hexaGap;

    private int offsetWidth;

    private int charWidth;
    private int charHeight;

    private int hexaCellWidth;
    private int hexaCellHeight;

    private int caretWidth;
    private int lineHeight;

    @Getter
    private JScrollBar scrollBar;

    @Getter
    @Setter
    private long caretPosition;

    @Getter
    @Setter
    private boolean hideCaret;

    @Setter
    private byte[] buffer = new byte[0];

    private boolean halfByteEdit = true;

    private boolean hexaMode = true;

    private long lastUpdateTime = 0;

    public BinaryHexaEditor(final BinaryStreamerView view) {
        this.view = view;
        this.editedBytes = new HashMap<>();
    }

    @Override
    public void initComponents() {
        setBackground(getHexaViewBackground());

        scrollBar = new JScrollBar(VERTICAL);
        scrollBar.addAdjustmentListener(e -> setLineOffset(e.getValue()));

        setFont(uiHandler.getDefaultMonospacedFont().deriveFont(13f));
        charWidth = getFontMetrics(getFont()).charWidth(' ');
        charHeight = getFontMetrics(getFont()).getAscent();

        hexaGap = charWidth;
        offsetWidth = 2 * offsetPadding + 8 * charWidth;
        hexaViewX = offsetWidth + padding;
        lineHeight = charHeight + 4;
        hexaCellWidth = 2 * charWidth;
        hexaCellHeight = lineHeight;
        caretWidth = hexaCellWidth + 5;

        initKeyHandling();
        initMouseHandling();
        initFocusHandling();
        initResizeHandling();
    }

    public void reset() {
        totalSize = ofNullable(view.getParentStreamer())
            .map(Streamer::getSize)
            .orElse(0L);
        offsetLineIndex = getLineIndex(offset);
        maxLineIndex = getLineIndex(totalSize);
        textWidth = bytesPerLine * charWidth + 2 * padding;
        linesCountPerPage = getHeight() / lineHeight;
        pageSize = bytesPerLine * linesCountPerPage;
        scrollBar.setMaximum((int) maxLineIndex);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (System.currentTimeMillis() < lastUpdateTime) {
            return;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        QUALITY.apply(g2d);

        g2d.setColor(getHexaViewBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(getForeground());
        g2d.drawLine(offsetWidth, 0, offsetWidth, getHeight());

        Stroke originalStroke = g2d.getStroke();
        g2d.setStroke(DASHED_STROKE);
        g2d.drawLine(getWidth() - textWidth, 0, getWidth() - textWidth, getHeight());
        g2d.setStroke(originalStroke);
        g2d.setColor(getTextViewBackground());
        g2d.fillRect(getWidth() - textWidth, 0, textWidth, getHeight());
        g2d.setColor(getOffsetBackground());
        g2d.fillRect(0, 0, offsetWidth, getHeight());
        range(0, buffer.length).forEach(index -> {
            long byteIndex = offset + index;
            byte byteValue = ofNullable(editedBytes.get(byteIndex))
                .orElseGet(() -> buffer[index]);

            int row = getLineIndex(index);
            int column = index % bytesPerLine;

            int x = hexaViewX + column * (hexaCellWidth + hexaGap);
            int y = row * hexaCellHeight;

            if (column == 0) {
                g2d.setColor(getOffsetForeground());
                g2d.drawString(getOffsetRepresentation(byteIndex), offsetPadding, y + charHeight);
            }

            int textX = getWidth() - (bytesPerLine - column) * charWidth - padding;
            g2d.setColor(getForeground());
            boolean drawCaret = !hideCaret && byteIndex == caretPosition;
            if (drawCaret) {
                if (hexaMode) {
                    g2d.setColor(getCaretBackground());
                    g2d.fillRect(x - 2, y, caretWidth, lineHeight);
                    g2d.setColor(getCaretDisabledBackground());
                    g2d.fillRect(textX, y, charWidth, lineHeight);
                } else {
                    g2d.setColor(getCaretDisabledBackground());
                    g2d.fillRect(x - 2, y, caretWidth, lineHeight);
                    g2d.setColor(getCaretBackground());
                    g2d.fillRect(textX, y, charWidth, lineHeight);
                }
                g2d.setColor(getCaretForeground());
            }

            g2d.drawString(getTextRepresentation(byteValue), textX, y + charHeight);
            if (byteValue == 0 && !drawCaret) {
                g2d.setColor(getNullForeground());
            }
            g2d.drawString(getHexaRepresentation(byteValue), x, y + charHeight);
            g2d.setColor(getForeground());
        });
    }

    protected void scrollToPage(long startLineIndex) {
        scrollBar.setValue((int) fitInRange(startLineIndex, 0, maxLineIndex));
    }

    @Override
    protected void processKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getID() == KEY_PRESSED) {
            actionController.runActionByKeyStroke(keyEvent);
        }

        if (isKeyAllowed(keyEvent)) {
            super.processKeyEvent(keyEvent);
        }
    }

    protected void initKeyHandling() {
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(getKeyStroke(VK_ENTER, 0), NO_ACTION_ID);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case VK_LEFT -> moveCaret(-1);
                    case VK_RIGHT -> moveCaret(1);
                    case VK_UP -> moveCaret(-bytesPerLine);
                    case VK_DOWN -> moveCaret(bytesPerLine);
                    case VK_PAGE_UP -> moveCaret(getPageUpDelta());
                    case VK_PAGE_DOWN -> moveCaret(getPageDownDelta());
                    case VK_HOME -> moveCaretToPosition(
                        e.isControlDown() ? 0 : getStartLineIndexForPosition(caretPosition));
                    case VK_END -> moveCaretToPosition(
                        e.isControlDown() ? totalSize - 1 : getEndLineIndexForPosition(caretPosition));
                }
            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == VK_DELETE) {
                    deleteCurrentByte();
                } else if (hexaMode) {
                    updateCurrentByteByHexaChar(keyEvent.getKeyChar());
                } else {
                    updateCurrentByteByTextChar(keyEvent.getKeyChar());
                }
            }
        });
    }

    protected void deleteCurrentByte() {
        byte newByte = 0;
        updateCurrentPosition(newByte);
        repaintAndUpdateView();
    }

    protected void updateCurrentByteByHexaChar(char character) {
        char upperCharacter = Character.toUpperCase(character);
        int charValue = Character.digit(upperCharacter, 16);
        if (charValue >= 0) {
            int currentBufferIndex = (int) (caretPosition - offset);
            byte currentByte = buffer[currentBufferIndex];
            byte newByte = halfByteEdit ?
                (byte) ((charValue << 4) | (currentByte & 0x0F)) :
                (byte) ((currentByte & 0xF0) | charValue);
            updateCurrentPosition(newByte);

            halfByteEdit = !halfByteEdit;
            if (halfByteEdit) {
                moveCaret(1);
            }
            repaintAndUpdateView();
        }
    }

    protected void updateCurrentByteByTextChar(char character) {
        if (isLetter(character)) {
            updateCurrentPosition((byte) character);
            moveCaret(1);
            repaintAndUpdateView();
        }
    }

    protected void updateCurrentPosition(Byte newByte) {
        int currentBufferIndex = (int) (caretPosition - offset);
        buffer[currentBufferIndex] = newByte;
        editedBytes.put(caretPosition, newByte);
    }

    protected void repaintAndUpdateView() {
        repaint();
        view.updateStreamerViewIdentifiers();
        view.updateStatus();
    }

    protected void moveCaret(long delta) {
        if (caretPosition + delta >= 0 && caretPosition + delta < totalSize) {
            moveCaretToPosition(caretPosition + delta);
        }
    }

    protected void moveCaretToPosition(long position) {
        caretPosition = position;
        ensureCaretIsVisible();
        repaint();
    }

    protected int getPageUpDelta() {
        int delta = -pageSize;
        while (caretPosition + delta < 0) {
            delta += bytesPerLine;
        }
        return delta;
    }

    protected int getPageDownDelta() {
        int delta = pageSize;
        while (caretPosition + delta >= totalSize) {
            delta -= bytesPerLine;
        }
        return delta;
    }

    protected void initMouseHandling() {
        addMouseWheelListener(e -> {
            long newOffsetLineIndex = offsetLineIndex + (long) e.getWheelRotation() * e.getScrollAmount();
            scrollToPage(newOffsetLineIndex);
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocus();

                int line = e.getY() / lineHeight;
                hexaMode = e.getX() < getWidth() - textWidth;
                int col = hexaMode ?
                    (e.getX() - offsetWidth) / (3 * charWidth) :
                    (e.getX() - (getWidth() - textWidth + padding)) / charWidth;

                if (col >= 0 && col < bytesPerLine) {
                    caretPosition = offset + (long) line * bytesPerLine + col;
                    repaint();
                }
            }
        });
    }

    protected void initFocusHandling() {
        setFocusable(true);
        setFocusCycleRoot(true);
        setFocusTraversalKeysEnabled(false);
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

    protected void initResizeHandling() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                bytesPerLine = (getWidth() - offsetWidth - padding) / (4 * charWidth + 1);
                reload();
            }
        });
    }

    public void setLineOffset(long lineOffset) {
        long newOffset = fitInRange(lineOffset, 0, maxLineIndex) * bytesPerLine;
        reload(newOffset);
    }

    public void reload() {
        reset();
        reload(offset);
    }

    public void reload(long newOffset) {
        if (newOffset != offset || buffer.length != offset + pageSize) {
            offset = newOffset;
            reset();
            LoadActionModel<?> loadActionModel = new LoadActionModel<>(view);
            loadActionModel.setOffset(offset);
            loadActionModel.setLimit(offset + pageSize);
            loadActionExecutor.execute(loadActionModel);
        }
    }

    protected void ensureCaretIsVisible() {
        long caretLineIndex = getLineIndex(caretPosition);

        if (caretLineIndex < offsetLineIndex) {
            scrollToPage(caretLineIndex);
        } else if (caretLineIndex >= offsetLineIndex + linesCountPerPage) {
            scrollToPage(caretLineIndex - linesCountPerPage + 1);
        }
    }

    protected int getLineIndex(long position) {
        return (int) position / bytesPerLine;
    }

    protected long getStartLineIndexForPosition(long position) {
        long caretLineIndex = getLineIndex(position);
        return caretLineIndex * bytesPerLine;
    }

    protected long getEndLineIndexForPosition(long position) {
        long caretLineIndex = getLineIndex(position);
        return min((caretLineIndex + 1) * bytesPerLine - 1, totalSize - 1);
    }

    @Override
    public void update(List<byte[]> buffers) {
        lastUpdateTime = System.currentTimeMillis();
        buffers.stream()
            .findFirst()
            .ifPresent(this::setBuffer);
    }

    protected String getOffsetRepresentation(long byteIndex) {
        return OFFSET_FORMAT.formatted(byteIndex);
    }

    protected String getHexaRepresentation(byte byteValue) {
        return HEXA_FORMAT.formatted(byteValue).toUpperCase();
    }

    protected String getTextRepresentation(byte byteValue) {
        char character = (char) (byteValue & 0xFF);
        return "" + (isLetter(character) ? character : '.');
    }

    protected boolean isKeyAllowed(KeyEvent keyEvent) {
        return isLetter(keyEvent.getKeyChar()) ||
            isMovementKey(keyEvent) ||
            isDeleteKey(keyEvent);
    }

    public Color getHexaViewBackground() {
        return uiHandler.getColor(TABLE_BACKGROUND);
    }

    public Color getTextViewBackground() {
        return uiHandler.getColor(TABLE_BACKGROUND);
    }

    public Color getOffsetBackground() {
        return uiHandler.getControlColor();
    }

    public Color getOffsetForeground() {
        return gray;
    }

    public Color getNullForeground() {
        return gray;
    }

    public Color getCaretDisabledBackground() {
        return lightGray;
    }

    public Color getCaretBackground() {
        return uiHandler.getColor(TEXT_PANE_SELECTION_BACKGROUND);
    }

    public Color getCaretForeground() {
        return inverseColor(uiHandler.getColor(EDITOR_PANE_CARET_FOREGROUND));
    }

    boolean isDirty() {
        return !editedBytes.isEmpty();
    }

    public void setDirty(boolean dirty) {
        if (dirty) {
            editedBytes.clear();
        }
    }
}
