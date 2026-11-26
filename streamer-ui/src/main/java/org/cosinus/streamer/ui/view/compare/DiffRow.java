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

package org.cosinus.streamer.ui.view.compare;

import java.util.List;

import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.DELETED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.INSERTED;

/**
 * A row of a side-by-side comparison of two texts.
 *
 * @param type        the type of the row
 * @param leftLine    the index of the line in the left text, or -1 if the row has no left line
 * @param rightLine   the index of the line in the right text, or -1 if the row has no right line
 * @param leftRanges  the changed character ranges in the left line of a changed row
 * @param rightRanges the changed character ranges in the right line of a changed row
 */
public record DiffRow(DiffType type,
                      int leftLine,
                      int rightLine,
                      List<Range> leftRanges,
                      List<Range> rightRanges) {

    public static final int NO_LINE = -1;

    public DiffRow(DiffType type, int leftLine, int rightLine) {
        this(type, leftLine, rightLine, List.of(), List.of());
    }

    public enum DiffType {
        /**
         * The same line in both texts
         */
        EQUAL,
        /**
         * A line only in the left text
         */
        DELETED,
        /**
         * A line only in the right text
         */
        INSERTED,
        /**
         * A line of the left text replaced by a line of the right text
         */
        CHANGED
    }

    /**
     * A range of characters in a line.
     *
     * @param start the index of the first character
     * @param end   the index after the last character
     */
    public record Range(int start, int end) {
    }

    public boolean hasLeftLine() {
        return leftLine != NO_LINE;
    }

    public boolean hasRightLine() {
        return rightLine != NO_LINE;
    }

    public boolean isDifferent() {
        return type != DiffType.EQUAL;
    }

    /**
     * @return this row with the left and right sides swapped
     */
    public DiffRow swap() {
        DiffType swappedType = switch (type) {
            case DELETED -> INSERTED;
            case INSERTED -> DELETED;
            default -> type;
        };
        return new DiffRow(swappedType, rightLine, leftLine, rightRanges, leftRanges);
    }
}
