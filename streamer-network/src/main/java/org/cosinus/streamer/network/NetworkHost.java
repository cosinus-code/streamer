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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static com.fasterxml.jackson.annotation.OptBoolean.FALSE;
import static org.cosinus.streamer.network.NetworkHost.TYPE;

/**
 * A host configured in network.json.
 * <p>
 * The "type" field of the host selects its configuration class,
 * registered by its network type with {@link NetworkConfigurationProvider#registerHostType(Class)}.
 * A host of a type which is not registered is read as {@link UnknownNetworkHost}.
 */
@JsonTypeInfo(use = NAME, include = EXISTING_PROPERTY, property = TYPE, visible = true,
    defaultImpl = UnknownNetworkHost.class, requireTypeIdForSubtypes = FALSE)
@JsonPropertyOrder(TYPE)
public interface NetworkHost {

    String TYPE = "type";

    /**
     * @return the network type of the host (e.g. "samba", "ftp"), as the type name of its configuration class
     */
    @JsonProperty(value = TYPE, access = READ_ONLY)
    String type();

    /**
     * @return the host name, as shown under the "Network" root streamer
     */
    String name();

    /**
     * @param newName the new host name
     * @return a copy of this host, with the new name
     */
    NetworkHost withName(String newName);

    /**
     * @return the host address
     */
    String host();

    /**
     * @return the folders shared by the host, or an empty list if the host has no folders level (e.g. ftp)
     */
    default List<? extends NetworkFolder> folders() {
        return List.of();
    }
}
