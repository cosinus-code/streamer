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

import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.streamer.ui.view.compare.CompareContentHandler;
import org.cosinus.swing.action.SwingActionWithModel;
import org.springframework.stereotype.Component;

import static org.cosinus.swing.image.icon.IconProvider.ICON_COMPARE;

/**
 * Compare side-by-side the text content of the current streamer of a view with the text in the clipboard.
 * <p>
 * The view is the one of the model location, or the current view when run without model.
 * The text content of the view is found by the {@link CompareContentHandler}.
 * If the view has no text streamer, or the clipboard has no text, nothing is compared and the user is informed.
 */
@Component
public class CompareWithClipboardAction implements SwingActionWithModel<CompareWithClipboardActionModel> {

    public static final String COMPARE_WITH_CLIPBOARD_ACTION = "compare-with-clipboard";

    public static final String COMPARE_WITH_CLIPBOARD_NOT_TEXT = "compare-with-clipboard-not-text";

    public static final String COMPARE_WITH_CLIPBOARD_NO_CLIPBOARD_TEXT = "compare-with-clipboard-no-clipboard-text";

    private final CompareContentHandler compareContentHandler;

    private final StreamerViewHandler streamerViewHandler;

    public CompareWithClipboardAction(final CompareContentHandler compareContentHandler,
                                      final StreamerViewHandler streamerViewHandler) {
        this.compareContentHandler = compareContentHandler;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public CompareWithClipboardActionModel createActionModel() {
        return new CompareWithClipboardActionModel(streamerViewHandler.getCurrentLocation());
    }

    @Override
    public void run(CompareWithClipboardActionModel actionModel) {
        compareContentHandler.findTextContent(actionModel.location())
            .ifPresentOrElse(
                streamerContent -> compareContentHandler.findClipboardContent()
                    .ifPresentOrElse(
                        clipboardContent -> compareContentHandler.compare(streamerContent, clipboardContent),
                        () -> compareContentHandler.showNothingToCompare(COMPARE_WITH_CLIPBOARD_NO_CLIPBOARD_TEXT)),
                () -> compareContentHandler.showNothingToCompare(COMPARE_WITH_CLIPBOARD_NOT_TEXT));
    }

    @Override
    public String getId() {
        return COMPARE_WITH_CLIPBOARD_ACTION;
    }

    @Override
    public String getIconName() {
        return ICON_COMPARE;
    }
}
