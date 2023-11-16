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
package org.cosinus.streamer.database.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.streamer.api.remote.JsonConnectionModelProvider;
import org.cosinus.swing.resource.ResourceResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DatabaseModelProvider extends JsonConnectionModelProvider<DatabaseConnectionModel> {

    private static final String DATABASE_CONNECTIONS_FILE_NAME = "database.json";

    protected DatabaseModelProvider(final ObjectMapper objectMapper,
                                    final Set<ResourceResolver> resourceResolvers) {
        super(objectMapper, DatabaseConnectionModel.class, resourceResolvers);
    }

    @Override
    protected String getJsonFileName() {
        return DATABASE_CONNECTIONS_FILE_NAME;
    }
}
