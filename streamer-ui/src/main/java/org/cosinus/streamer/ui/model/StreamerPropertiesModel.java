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

package org.cosinus.streamer.ui.model;

import lombok.Getter;
import lombok.Setter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.DateValue;
import org.cosinus.streamer.api.value.MemoryValue;
import org.cosinus.swing.file.FileHandler;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.ui.UIModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.icon.IconSize.X64;

public class StreamerPropertiesModel implements UIModel {

    private static final String STREAMER_NAME = "streamer-name";

    private static final String STREAMER_ICON = "streamer-icon";

    private static final String STREAMER_TYPE = "streamer-type";

    private static final String STREAMER_SIZE = "streamer-size";

    private static final String STREAMER_LOCATION = "streamer-location";

    private static final String STREAMER_LAST_MODIFIED = "streamer-last-modified";

    private static final Set<String> KEYS = Set.of(
        STREAMER_NAME,
        STREAMER_TYPE,
        STREAMER_SIZE,
        STREAMER_LOCATION,
        STREAMER_LAST_MODIFIED);

    @Autowired
    private IconHandler iconHandler;

    @Autowired
    private FileHandler fileHandler;

    private final Streamer<?> streamer;

    @Getter
    @Setter
    private String newName;

    @Getter
    private File iconFile;

    public StreamerPropertiesModel(final Streamer<?> streamer) {
        injectContext(this);
        this.streamer = streamer;
    }

    @Override
    public Set<String> keys() {
        return KEYS;
    }

    @Override
    public void putValue(String key, Object value) {
        if (key.equals(STREAMER_NAME)) {
            ofNullable(value)
                .map(Object::toString)
                .filter(not(String::isBlank))
                .ifPresent(this::setNewName);
        }
    }

    @Override
    public Object getValue(String key) {
        return switch (key) {
            case STREAMER_NAME -> streamer.getName();
            case STREAMER_ICON -> streamer.getIconName();
            case STREAMER_TYPE -> streamer.getTypeDescription();
            case STREAMER_LOCATION -> ofNullable(streamer.getPath())
                .map(Path::getParent)
                .map(Objects::toString)
                .orElse("");
            case STREAMER_LAST_MODIFIED -> new DateValue(streamer.lastModified());
            case STREAMER_SIZE -> new MemoryValue(streamer.getSize());
            default -> null;
        };
    }

    @Override
    public Icon getIcon() {
        return ofNullable(streamer.getIconName())
            .flatMap(iconName -> iconHandler.findIconByName(iconName, X64, false))
            .or(() -> iconHandler.findIconByFile(createItemFile(streamer), X64))
            .orElse(null);
    }

    @Override
    public void setIconFile(final File iconFile) {
        this.iconFile = iconFile;
    }

    private File createItemFile(Streamer<?> streamer) {
        return fileHandler.createVirtualFile(streamer.getPath(), streamer.getName(), streamer.isParent());
    }
}
