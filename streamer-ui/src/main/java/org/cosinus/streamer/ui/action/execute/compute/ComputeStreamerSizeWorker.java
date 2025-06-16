/*
 * Copyright 2025 Cosinus Software
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
package org.cosinus.streamer.ui.action.execute.compute;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerSizeHandler;
import org.cosinus.streamer.api.worker.Worker;
import org.cosinus.streamer.api.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Optional.ofNullable;

/**
 * Worker for computing a streamer size
 */
public class ComputeStreamerSizeWorker extends Worker<WorkerModel<Void>, Void> {

    private final Streamer<?> streamer;

    @Autowired
    private StreamerSizeHandler streamerSizeHandler;

    public ComputeStreamerSizeWorker(final ComputeStreamerSizeModel actionModel) {
        super(actionModel, actionModel);
        this.streamer = actionModel.getStreamer();
    }

    @Override
    protected void doWork() {
        // The logic is too complicated.
        // There should be an easier way to compute a folder size with 2 constraints:
        // 1. cache also the computed size for the all sub-folders.
        // 2. cache the intermediate size of all folders containing a leaf, when adding the size of that leaf.
        computeStreamerSize(streamer);
    }

    private long computeStreamerSize(Streamer<?> streamer) {
        return streamer instanceof ParentStreamer<?> parentStreamer ?
            computeParentStreamerSize(parentStreamer) :
            streamer.getSize();
    }

    private long computeParentStreamerSize(ParentStreamer<?> parentStreamer) {
        streamerSizeHandler.startComputingStreamerSize(parentStreamer);
        try {
            return parentStreamer
                .stream()
                .map(streamer -> new StreamerSize(streamer, computeStreamerSize(streamer)))
                .filter(sizeToAdd -> sizeToAdd.size() > 0)
                .reduce(zero(parentStreamer), this::sum)
                .size();
        } finally {
            streamerSizeHandler.stopComputingStreamerSize(parentStreamer);
        }
    }

    private StreamerSize sum(StreamerSize sizeToAddTo, StreamerSize sizeToAdd) {
        long totalSize = sizeToAddTo.size() + sizeToAdd.size();
        streamerSizeHandler.cacheStreamerSize(sizeToAddTo.streamer(), totalSize);
        if (!sizeToAdd.streamer().isParent()) {
            addSizeToAllParents(sizeToAddTo.streamer(), sizeToAdd.size());
        }
        publish();
        return new StreamerSize(sizeToAddTo.streamer(), totalSize);
    }

    private void addSizeToAllParents(Streamer<?> streamer, long sizeToAdd) {
        ofNullable(streamer.getParent())
            .filter(streamerSizeHandler::isStreamerSizeComputing)
            .ifPresent(parent -> {
                long previousTotalSize = streamerSizeHandler.getSize(parent);
                streamerSizeHandler.cacheStreamerSize(parent, previousTotalSize + sizeToAdd);
                addSizeToAllParents(parent, sizeToAdd);
            });
    }

    private StreamerSize zero(Streamer<?> streamer) {
        return new StreamerSize(streamer, 0);
    }

    private record StreamerSize(Streamer<?> streamer, long size) {
    }
}
