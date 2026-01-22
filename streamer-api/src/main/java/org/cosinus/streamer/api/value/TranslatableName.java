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
package org.cosinus.streamer.api.value;

import lombok.Getter;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class TranslatableName {

    @Autowired
    public Translator translator;

    private final String key;

    @Getter
    private String name;

    private TranslatableName(String key, String name) {
        injectContext(this);
        this.key = key;
        this.name = name;

        if (name == null) {
            translate();
        }
    }

    public static TranslatableName name(String name) {
        return new TranslatableName(null, name);
    }

    public static TranslatableName translatableName(String key) {
        return new TranslatableName(key, null);
    }

    public static List<TranslatableName> translatableNames(String... keys) {
        return stream(keys)
            .map(key -> new TranslatableName(key, null))
            .collect(Collectors.toList());
    }

    public void translate() {
        if (key != null) {
            name = translator.translate(key);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranslatableName that)) return false;
        return key != null ? Objects.equals(key, that.key) : Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return key != null ? Objects.hash(key) : Objects.hash(name);
    }
}
