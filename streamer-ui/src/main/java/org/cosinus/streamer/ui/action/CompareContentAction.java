/*
 * Copyright 2025 Cosinus Software
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

package org.cosinus.streamer.ui.action;

import org.apache.commons.lang3.tuple.Pair;
import org.cosinus.streamer.ui.view.compare.CompareContentHandler;
import org.cosinus.swing.action.SwingAction;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_C;
import static org.cosinus.streamer.ui.view.PanelLocation.LEFT;
import static org.cosinus.streamer.ui.view.PanelLocation.RIGHT;
import static org.cosinus.swing.image.icon.IconProvider.ICON_COMPARE;

/**
 * Compare side-by-side the text contents of the current streamers of the left and right views.
 * <p>
 * The text contents are found by the {@link CompareContentHandler}.
 * If one of the views has no text streamer, nothing is compared and the user is informed.
 */
@Component
public class CompareContentAction implements SwingAction {

    public static final String COMPARE_CONTENT_ACTION = "menu-edit-compare-content";

    public static final String COMPARE_CONTENT_NOT_TEXT = "compare-content-not-text";

    private final CompareContentHandler compareContentHandler;

    private final ApplicationUIHandler uiHandler;

    public CompareContentAction(final CompareContentHandler compareContentHandler,
                                final ApplicationUIHandler uiHandler) {
        this.compareContentHandler = compareContentHandler;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        compareContentHandler.findTextContent(LEFT)
            .flatMap(leftContent -> compareContentHandler.findTextContent(RIGHT)
                .map(rightContent -> Pair.of(leftContent, rightContent)))
            .ifPresentOrElse(
                textContexts -> compareContentHandler.compare(textContexts.getLeft(), textContexts.getRight()),
                () -> compareContentHandler.showNothingToCompare(COMPARE_CONTENT_NOT_TEXT));
    }

    @Override
    public String getId() {
        return COMPARE_CONTENT_ACTION;
    }

    @Override
    public String getIconName() {
        return ICON_COMPARE;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_C, uiHandler.getControlDownKeyMask() | SHIFT_DOWN_MASK));
    }
}
