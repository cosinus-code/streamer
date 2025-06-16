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

import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class TranslatableName {

    @Autowired
    public Translator translator;

    private String key;

    private String name;

    public TranslatableName(String name) {
        this(null, name);
    }

    public TranslatableName(String key, String name) {
        injectContext(this);
        this.key = key;
        this.name = name;

        if (name == null) {
            translate();
        }
    }

    public String name() {
        return name;
    }

    public void translate() {
        if (key != null) {
            name = translator.translate(key);
        }
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
