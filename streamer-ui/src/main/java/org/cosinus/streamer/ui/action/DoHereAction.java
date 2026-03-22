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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.SwingActionWithModel;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.file.FileMainStreamer.FILE_PROTOCOL;
import static org.cosinus.swing.file.FileHandler.PROTOCOL_MARK;

public abstract class DoHereAction implements SwingActionWithModel<DoHereModel> {

    private final StreamerHandler streamerHandler;

    private final ActionExecutors actionExecutors;

    protected DoHereAction(final StreamerHandler streamerHandler,
                           final ActionExecutors actionExecutors) {
        this.streamerHandler = streamerHandler;
        this.actionExecutors = actionExecutors;
    }

    @Override
    public void run(DoHereModel doHereModel) {
        ofNullable(doHereModel.getDestinationView())
            .map(streamerView ->
                findDestination(streamerView, doHereModel.isUseSelectedItemAsDestination()))
            .map(currentStreamer -> Optional.of(currentStreamer)
                .map(ParentStreamer.class::cast)
                .orElseGet(currentStreamer::getParent))
            .ifPresent(destination -> {
                List<Streamer<?>> streamers = findStreamersForAction(doHereModel.getPaths());
                if (!streamers.isEmpty()) {
                    ParentStreamer<?> commonParent = findCommonParent(streamers);
                    ActionModel actionModel = createActionModel(
                        streamers,
                        doHereModel.getSourceView(), commonParent,
                        doHereModel.getDestinationView(), destination);
                    actionExecutors.execute(actionModel);
                }
            });
    }

    protected Streamer<?> findDestination(StreamerView<?> viewDestination, boolean useSelectedItemAsDestination) {
        return useSelectedItemAsDestination ?
            viewDestination
                .getSelectedItems()
                .stream()
                .filter(item -> ParentStreamer.class.isAssignableFrom(item.getClass()))
                .map(Streamer.class::cast)
                .findFirst()
                .orElseGet(viewDestination::getParentStreamer) :
            viewDestination.getParentStreamer();
    }

    protected List<Streamer<?>> findStreamersForAction(List<String> streamerPaths) {
        return streamerPaths
            .stream()
            .map(path -> path.contains(PROTOCOL_MARK) ? path : FILE_PROTOCOL + path)
            .map(streamerHandler::findStreamerForUrlPath)
            .filter(Optional::isPresent)
            .<Streamer<?>>map(Optional::get)
            .toList();
    }

    protected ParentStreamer<?> findCommonParent(List<Streamer<?>> streamers) {
        return streamers.stream()
            .findFirst()
            .flatMap(firstStreamer -> firstStreamer.parentsStream()
                .filter(parent -> streamers.stream()
                    .allMatch(streamer -> streamer.getPath().startsWith(parent.getPath())))
                .findFirst()
            )
            .orElse(null);
    }

    protected abstract ActionModel createActionModel(List<Streamer<?>> streamers,
                                                     StreamerView<?> sourceView,
                                                     ParentStreamer<?> source,
                                                     StreamerView<?> destinationView,
                                                     ParentStreamer<?> destination);
}
