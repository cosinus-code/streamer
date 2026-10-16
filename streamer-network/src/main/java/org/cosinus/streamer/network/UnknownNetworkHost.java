/*
 * Copyright 2026 Cosinus Software
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


package org.cosinus.streamer.network;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A host of a network type which is not registered (e.g. its module is missing or disabled),
 * kept with all its fields, so it is saved back unchanged in network.json.
 */
public class UnknownNetworkHost implements NetworkHost {

    private static final String NAME = "name";

    private static final String HOST = "host";

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public UnknownNetworkHost() {
    }

    /**
     * @param type the network type
     * @param name the host name
     * @param host the host address
     */
    public UnknownNetworkHost(String type, String name, String host) {
        fields.put(TYPE, type);
        fields.put(NAME, name);
        fields.put(HOST, host);
    }

    @JsonAnySetter
    public void setField(String name, Object value) {
        fields.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getFields() {
        return fields;
    }

    @Override
    @JsonIgnore
    public String type() {
        return text(TYPE);
    }

    @Override
    public String name() {
        return text(NAME);
    }

    @Override
    public String host() {
        return text(HOST);
    }

    @Override
    public UnknownNetworkHost withName(String newName) {
        UnknownNetworkHost renamed = new UnknownNetworkHost();
        renamed.fields.putAll(fields);
        renamed.fields.put(NAME, newName);
        return renamed;
    }

    private String text(String field) {
        return Objects.toString(fields.get(field), null);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof UnknownNetworkHost that && fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }
}
