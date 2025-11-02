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

package org.cosinus.streamer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.cosinus.streamer.api.value.DateValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class KeyValueStreamer implements JsonStreamer {

    public static final String KEY_VALUE_ICON_NAME = "application-json";

    private final ParentStreamer<?> parent;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Translator translator;

    private final Pair<String, Object> keyValue;

    protected List<Value> details;

    public KeyValueStreamer(final ParentStreamer<?> parent,
                            String key, Object value) {
        this(parent, Pair.of(key, value));
    }

    public KeyValueStreamer(final ParentStreamer<?> parent,
                            Map.Entry<String, Object> keyValue) {
        this(parent, Pair.of(keyValue.getKey(), keyValue.getValue()));
    }

    public KeyValueStreamer(final ParentStreamer<?> parent,
                            Pair<String, Object> keyValue) {
        injectContext(this);
        this.parent = parent;
        this.keyValue = keyValue;
        this.details = asList(
            new TextValue(translator.translate(keyValue.getKey())),
            ofNullable(keyValue.getValue())
                .map(value -> value instanceof Date ?
                    new DateValue(value) :
                    new TextValue(value))
                .orElseGet(() -> new TextValue("-"))
            //new TextValue(ofNullable(keyValue.getValue()).orElse("-"))
        );
    }

    @Override
    public String getName() {
        return translator.translate(keyValue.getKey());
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(getName());
    }

    @Override
    public ParentStreamer<?> getParent() {
        return parent;
    }

    @Override
    public String getIconName() {
        return KEY_VALUE_ICON_NAME;
    }

    @Override
    public Object getSource() {
        return keyValue;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public String getType() {
        return "json";
    }

    @Override
    public boolean isTextCompatible() {
        return true;
    }

    @Override
    public List<Value> details() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyValueStreamer that)) return false;
        return Objects.equals(keyValue, that.keyValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(keyValue);
    }

    @Override
    public String getDescription() {
        return details().get(1).toString();
    }
}
