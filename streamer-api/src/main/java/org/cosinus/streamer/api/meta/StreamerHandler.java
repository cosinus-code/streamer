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

package org.cosinus.streamer.api.meta;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Component
public class StreamerHandler {

    private static final Logger LOG = LogManager.getLogger(StreamerHandler.class);

    public static final String EXPANDER_SEPARATOR = "#";

    private final MetaStreamer metaStreamer;

    private final BinaryExpanderHandler binaryExpanderHandler;

    private final Streamer<?> defaultStreamer;

    public StreamerHandler(List<MainStreamer> mainStreamers,
                           BinaryExpanderHandler binaryExpanderHandler,
                           @Value("streamer.default") String defaultStreamerName) {
        mainStreamers.forEach(this::setStreamerName);
        this.metaStreamer = new MetaStreamer(mainStreamers);
        this.binaryExpanderHandler = binaryExpanderHandler;

        this.defaultStreamer = mainStreamers
            .stream()
            .filter(streamer -> streamer.getName().equals(defaultStreamerName))
            .findFirst()
            .map(Streamer.class::cast)
            .orElse(metaStreamer);
    }

    private void setStreamerName(MainStreamer streamer) {
        RootStreamer rootStreamer = streamer.getClass().getAnnotation(RootStreamer.class);
        streamer.setName(Optional.of(rootStreamer.name())
                             .filter(name -> !name.isEmpty())
                             .orElseGet(rootStreamer::value));
    }

    public Streamer getStreamer(String urlPath) {
        return ofNullable(urlPath)
            .flatMap(this::findStreamerForUrlPath)
            .orElseGet(this::getDefaultStreamer);
    }

    public Streamer<?> getDefaultStreamer() {
        return defaultStreamer;
    }

    public Optional<Streamer> findStreamerForUrlPath(String urlPath) {
        try {
            String[] paths = urlPath.split(EXPANDER_SEPARATOR);

            if (paths.length < 2) {
                return metaStreamer.findByUrlPath(urlPath)
                    .or(() -> findStreamer(urlPath));
            }

            String mainUrlPath = paths[0];
            String pathInsideExpandedStreamer = paths[1];
            return metaStreamer.findByUrlPath(mainUrlPath)
                .or(() -> findStreamer(mainUrlPath))
                .filter(streamer -> BinaryStreamer.class.isAssignableFrom(streamer.getClass()))
                .map(BinaryStreamer.class::cast)
                .flatMap(binaryStreamer -> findExpandedStreamer(binaryStreamer, pathInsideExpandedStreamer));
        } catch (Exception ex) {
            LOG.error("Failed to find streamer for url {}", urlPath, ex);
            return empty();
        }
    }

    private Optional<Streamer> findStreamer(String urlPath) {
        return metaStreamer.stream()
            .filter(streamer -> streamer.isCompatible(urlPath))
            .findFirst()
            .flatMap(streamer -> streamer.findByUrlPath(urlPath));
    }

    private Optional<Streamer> findExpandedStreamer(BinaryStreamer binaryStreamer, String pathInsideExpandedStreamer) {
        return binaryExpanderHandler.findStreamExpander(binaryStreamer.getType())
            .map(binaryExpander -> binaryExpander.expand(binaryStreamer))
            .flatMap(expandedStreamer -> expandedStreamer.find(pathInsideExpandedStreamer));
    }
}
