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

package org.cosinus.streamer.strava;

import lombok.Getter;
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.security.AuthorizedClient;
import org.cosinus.swing.security.LocalStorageOAuth2AuthorizedClientService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.strava.StravaUserStreamer.*;
import static org.cosinus.streamer.strava.client.StravaClient.STRAVA_CLIENT_ID;

@Getter
@RootStreamer("Strava")
@StravaComponent
public class StravaMainStreamer extends MainStreamer<StravaUserStreamer> {

    public static final String STRAVA_PROTOCOL = "strava://";

    public static final String STRAVA_ICON_NAME = "strava";

    public static final String ATHLETE = "athlete";

    private final LocalStorageOAuth2AuthorizedClientService localStorageOAuth2AuthorizedClientService;

    private final FilesystemResourceResolver resourceResolver;

    protected List<TranslatableName> detailNames;

    public StravaMainStreamer(
        final LocalStorageOAuth2AuthorizedClientService localStorageOAuth2AuthorizedClientService,
        final FilesystemResourceResolver resourceResolver) {

        this.localStorageOAuth2AuthorizedClientService = localStorageOAuth2AuthorizedClientService;
        this.resourceResolver = resourceResolver;
    }

    @Override
    public Stream<StravaUserStreamer> stream() {
        return localStorageOAuth2AuthorizedClientService.getAuthorizedClientsMap(STRAVA_CLIENT_ID)
            .entrySet()
            .stream()
            .map(entry -> new StravaUserStreamer(
                entry.getKey(),
                ofNullable(entry.getValue())
                    .map(AuthorizedClient::getDetails)
                    .map(details -> details.get(ATHLETE))
                    .filter(details -> details instanceof Map)
                    .map(details -> (Map<String, Object>) details)
                    .orElseGet(HashMap::new)));
    }

    @Override
    public String getProtocol() {
        return STRAVA_PROTOCOL;
    }

    @Override
    public String getIconName() {
        return STRAVA_ICON_NAME;
    }

    @Override
    public List<TranslatableName> detailNames() {
        if (detailNames == null) {
            detailNames = TranslatableName.translatableNames(
                USERNAME,
                FIRST_NAME,
                LAST_NAME,
                CITY,
                COUNTRY
            );
        }
        return detailNames;
    }
}
