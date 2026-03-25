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

package org.cosinus.streamer.api.file;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.*;
import org.cosinus.swing.file.FileHandler;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static org.cosinus.streamer.api.TextStreamer.TEXT_VIEW_ID;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class BaseFileStreamer<T> implements Streamer<T> {

    protected static final int DETAIL_INDEX_NAME = 0;
    protected static final int DETAIL_INDEX_SIZE = 2;

    public static final String BINARY_VIEW_ID = "binary";

    public static final String IMAGE_VIEW_ID = "image";

    @Autowired
    protected FileHandler fileHandler;

    @Autowired
    private MimeTypeResolver mimeTypeResolver;

    protected final List<TranslatableName> detailNames;

    protected List<Value> details;

    public BaseFileStreamer() {
        injectContext(this);
        detailNames = TranslatableName.translatableNames(
            DETAIL_KEY_NAME,
            DETAIL_KEY_TYPE,
            DETAIL_KEY_SIZE,
            DETAIL_KEY_TIME);
    }

    @Override
    public String getDescription() {
        return Stream.of(
                getTypeDescription(),
                getDetailedSize())
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(not(String::isBlank))
            .collect(joining(", "));
    }

    public String getTypeDescription() {
        return fileHandler.getTypeDescription(getPath(), isParent())
            .orElse("");
    }

    public String getDetailedSize() {
        return ofNullable(details().get(DETAIL_INDEX_SIZE))
            .map(Object::toString)
            .orElse(null);
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public List<Value> details() {
        init();
        return details;
    }

    public void init() {
        if (details == null) {
            details = asList(
                new TextValue(getName()),
                new TextValue(getTypeDescription()),
                new MemoryValue(getSize(), isSizeComputing()),
                new DateValue(lastModified()));
        }
    }

    protected boolean isSizeComputing() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public void reset() {
        details = null;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public String getViewId() {
        return isImageCompatible() ? IMAGE_VIEW_ID :
            isTextCompatible() ? TEXT_VIEW_ID :
                BINARY_VIEW_ID;
    }

    public boolean isImageCompatible() {
        return mimeTypeResolver.isImageCompatible(getPath());
    }

    public boolean isTextCompatible() {
        return mimeTypeResolver.isTextCompatible(getPath()) ||
            mimeTypeResolver.hasUnknownMimeType(getPath());
    }

}
