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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import static java.util.Optional.ofNullable;

public class BinaryDocumentFilter extends DocumentFilter {

    @Getter
    private boolean skipNavigation;

    @Override
    public void replace(FilterBypass filterBypass, int offset, int length,
                        String text, AttributeSet attributes) throws BadLocationException {

        if (text != null && text.length() == 1) {
            ofNullable(filterBypass.getDocument())
                .filter(doc -> offset < doc.getLength())
                .ifPresent(doc -> {
                    try {
                        skipNavigation = true;
                        filterBypass.remove(offset, 1);
                        skipNavigation = false;
                    } catch (BadLocationException e) {
                        // ignore
                    }
                });
        }

        super.replace(filterBypass, offset, length, text, attributes);
    }

    @Override
    public void remove(FilterBypass filterBypass, int offset, int length) {
        if (length == 1) {
            ofNullable(filterBypass.getDocument())
                .filter(doc -> offset + 1 < doc.getLength())
                .ifPresent(doc -> {
                    try {
                        skipNavigation = true;
                        filterBypass.replace(offset + 1, length, "0", null);
                        skipNavigation = false;
                    } catch (BadLocationException e) {
                        // ignore
                    }
                });
        }
    }
}
