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

import javax.swing.text.Document;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position.Bias;

public class BinaryNavigationFilter extends NavigationFilter {

    private final BinaryView binaryView;

    public BinaryNavigationFilter(final BinaryView binaryView) {
        this.binaryView = binaryView;
    }

    @Override
    public void setDot(FilterBypass filterBypass, int position, Bias bias) {
        if (binaryView.isNavigationAllowed()) {
            super.setDot(filterBypass, adjustDot(position), bias);
        }
    }

    @Override
    public void moveDot(FilterBypass filterBypass, int position, Bias bias) {
        if (binaryView.isNavigationAllowed()) {
            super.moveDot(filterBypass, adjustDot(position), bias);
        }
    }

    private int adjustDot(int position) {
        Document document = binaryView.getDocument();
        int nextPosition = position;
        if (position > binaryView.getCaretPosition()) {
            while (binaryView.isInvalidCharAtPosition(nextPosition) && nextPosition + 1 < document.getLength()) {
                nextPosition++;
            }
            return nextPosition < document.getLength() ? nextPosition : binaryView.getCaretPosition();
        } else {
            while (binaryView.isInvalidCharAtPosition(nextPosition) && nextPosition - 1 >= 0) {
                nextPosition--;
            }
            return nextPosition >= 0 ? nextPosition : binaryView.getCaretPosition();
        }
    }
}
