/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.cosinus.streamer.ui.view;

import java.util.Optional;

import static java.util.Arrays.stream;
import static org.cosinus.swing.image.icon.IconProvider.*;

public enum View {

    ICON("view-icon", ICON_VIEW_ICON),
    GRID("view-grid", ICON_VIEW_GRID),
    LIST("view-list", ICON_VIEW_LIST),
    TREE("view-tree", ICON_VIEW_TREE);

    private final String key;

    private final String iconName;

    View(String key, String iconName) {
        this.key = key;
        this.iconName = iconName;
    }

    public String getKey() {
        return key;
    }

    public String getIconName() {
        return iconName;
    }

    public static Optional<View> findByName(String name) {
        return stream(values())
            .filter(value -> value.name().equalsIgnoreCase(name))
            .findFirst();
    }
}
