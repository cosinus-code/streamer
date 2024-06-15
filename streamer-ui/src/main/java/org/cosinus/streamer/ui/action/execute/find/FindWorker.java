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
package org.cosinus.streamer.ui.action.execute.find;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.ui.action.execute.Worker;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerViewStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

public class FindWorker extends Worker<FindWorkerModel, Streamer<?>> {

    @Autowired
    private StreamerViewStorage streamerViewStorage;

    @Autowired
    private StreamerHandler streamerHandler;

    @Autowired
    private BinaryExpanderHandler binaryExpanderHandler;

    private final FindActionModel findActionModel;

    private final Streamer streamerToFind;

    protected FindWorker(final FindActionModel findActionModel, final Streamer<?> streamerToFind) {
        super(findActionModel, new FindWorkerModel());
        this.findActionModel = findActionModel;
        this.streamerToFind = streamerToFind;
    }

    @Override
    protected void doWork() {

        Streamer streamer = findStreamer()
            .map(binaryExpanderHandler::expandStreamer)
            .orElse(null);

        publish(streamer);
    }

    private Optional<Streamer> findStreamer() {
        return ofNullable(streamerToFind)
            .or(() -> loadLastStreamer(findActionModel.getLocation()))
            .or(() -> ofNullable(streamerHandler.getDefaultStreamer()))
            .map(this::checkIfStreamerExist);
    }

    private Optional<Streamer> loadLastStreamer(PanelLocation location) {
        return streamerViewStorage.loadLastLoadedStreamer(location)
            .map(urlPath -> streamerHandler.getStreamer(urlPath));
    }

    private Streamer<?> checkIfStreamerExist(Streamer<?> streamerToCheck) {
        return !streamerToCheck.exists() ? findFirstAncestorAlive(streamerToCheck) : streamerToCheck;
    }

    private Streamer<?> findFirstAncestorAlive(Streamer streamer) {
        return ofNullable(streamer.getParent())
            .filter(not(Streamer::exists))
            .map(this::findFirstAncestorAlive)
            .orElse(streamer.getParent());
    }
}
