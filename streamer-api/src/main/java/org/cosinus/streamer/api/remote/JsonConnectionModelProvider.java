/*
 * Copyright 2020 Cosinus Software
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

package org.cosinus.streamer.api.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.convert.JsonFileConverter;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.resource.ResourceType;

import java.util.Map;
import java.util.Set;

import static org.cosinus.swing.resource.ResourceType.CONF;

/**
 * FTP connection provider
 */
public abstract class JsonConnectionModelProvider<M>
    extends JsonFileConverter<M> implements ConnectionModelProvider<M> {

    protected JsonConnectionModelProvider(final ObjectMapper objectMapper,
                                          final Class<M> connectionModelType,
                                          final Set<ResourceResolver> resourceResolvers) {

        super(objectMapper, connectionModelType, resourceResolvers);
    }

    public Map<String, M> getConnectionModelsMap() {
        return convertToMapOfModels(getJsonFileName())
            .orElse(null);
    }

    protected abstract String getJsonFileName();

    @Override
    protected ResourceType resourceLocator() {
        return CONF;
    }
}
