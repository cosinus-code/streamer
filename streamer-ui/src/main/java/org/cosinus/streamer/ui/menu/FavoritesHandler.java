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

package org.cosinus.streamer.ui.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.convert.JsonFileConverter;
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.error.SpringSwingException;
import org.cosinus.swing.resource.ResourceLocator;
import org.cosinus.swing.resource.ResourceResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.cosinus.swing.resource.ResourceType.CONF;

@Component
public class FavoritesHandler extends JsonFileConverter<String> {

    private static final String FAVORITES_FILE_NAME = "favorites.json";

    private List<String> favorites;

    protected FavoritesHandler(final ObjectMapper objectMapper,
                               final Set<ResourceResolver> resourceResolvers) {
        super(objectMapper, String.class, resourceResolvers);
        initFavorites();
    }

    public List<String> getFavorites() {
        if (favorites == null) {
            initFavorites();
        }

        return favorites;
    }

    private void initFavorites() {
        try {
            favorites = convertToListOfModels(FAVORITES_FILE_NAME)
                .orElseGet(ArrayList::new);
        } catch (JsonConvertException ex) {
            favorites = new ArrayList<>();
        }
    }

    public void addFavorite(final String favorite) {
        favorites.add(favorite);
        try {
            saveModelsList(FAVORITES_FILE_NAME, favorites);
        } catch (IOException ex) {
            throw new SpringSwingException("Failed to save favorites.", ex);
        }
    }

    public void removeFavorite(final String favorite) {
        favorites.remove(favorite);
        try {
            saveModelsList(FAVORITES_FILE_NAME, favorites);
        } catch (IOException ex) {
            throw new SpringSwingException("Failed to save favorites.", ex);
        }
    }

    public boolean isFavorite(String address) {
        return favorites.contains(address);
    }

    @Override
    protected ResourceLocator resourceLocator() {
        return CONF;
    }
}
