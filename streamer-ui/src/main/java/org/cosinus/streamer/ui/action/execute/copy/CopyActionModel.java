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

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.swing.action.execute.ActionModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.UUID.randomUUID;

/**
 * Encapsulates the model of the copy streamers action
 */
public class CopyActionModel<S extends Streamer<?>, T extends Streamer<?>> extends ActionModel {

    private static final String COPY_ACTION_NAME = "act-copy";

    private static final String MOVE_ACTION_NAME = "act-move";

    private static final String PACK_ACTION_NAME = "act-pack";

    private StreamerFilter sourceFilter = streamer -> true;

    private List<Streamer<?>> streamersToCopy;

    private ParentStreamer<S> source;

    private ParentStreamer<T> destination;

    private Path targetPath;

    private String packType;

    private boolean shouldPack;

    public CopyActionModel(String actionName) {
        super(randomUUID().toString(), actionName);
    }

    public static <S extends Streamer<?>, T extends Streamer<?>>
    CopyActionModel<S, T> copy(List<Streamer<?>> streamersToCopy) {
        return new CopyActionModel<S, T>(COPY_ACTION_NAME)
            .setStreamersToCopy(streamersToCopy);
    }

    public static <S extends Streamer<?>, T extends Streamer<?>>
    CopyActionModel<S, T> move(List<Streamer<?>> streamersToCopy) {
        return new CopyActionModel<S, T>(MOVE_ACTION_NAME)
            .setStreamersToCopy(streamersToCopy);
    }

    public static <S extends Streamer<?>, T extends Streamer<?>>
    CopyActionModel<S, T> pack(List<Streamer<?>> streamersToCopy) {
        return new CopyActionModel<S, T>(PACK_ACTION_NAME)
            .setStreamersToCopy(streamersToCopy)
            .pack();
    }

    public Path getTargetPath() {
        return targetPath;
    }

    public CopyActionModel<S, T> toTargetPath(Path targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    public CopyActionModel<S, T> toTargetPath(String targetPath) {
        return toTargetPath(Paths.get(targetPath));
    }

    public String getPackType() {
        return packType;
    }

    public CopyActionModel<S, T> withPackType(String packType) {
        this.packType = packType;
        return this;
    }

    public ParentStreamer<S> getSource() {
        return source;
    }

    public StreamerFilter getSourceFilter() {
        return sourceFilter;
    }

    public CopyActionModel<S, T> setSourceFilter(StreamerFilter sourceFilter) {
        this.sourceFilter = sourceFilter;
        return this;
    }

    public CopyActionModel<S, T> setStreamersToCopy(List<Streamer<?>> streamersToCopy) {
        this.streamersToCopy = streamersToCopy;
        this.sourceFilter = this.streamersToCopy::contains;
        return this;
    }

    public List<Streamer<?>> getStreamersToCopy() {
        return streamersToCopy;
    }

    public CopyActionModel<S, T> from(ParentStreamer<S> source) {
        this.source = source;
        return this;
    }

    public ParentStreamer<T> getDestination() {
        return destination;
    }

    public CopyActionModel<S, T> to(ParentStreamer<T> destination) {
        this.destination = destination;
        this.targetPath = destination.getPath();
        return this;
    }

    public boolean shouldPackStreamers() {
        return shouldPack;
    }

    public CopyActionModel<S, T> pack() {
        this.shouldPack = true;
        return this;
    }
}
