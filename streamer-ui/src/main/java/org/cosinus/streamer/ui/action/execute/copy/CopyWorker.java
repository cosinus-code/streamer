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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.Pipeline;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.stream.pipeline.error.AbortPipelineConsumeException;
import org.cosinus.streamer.ui.action.execute.SimpleWorker;
import org.cosinus.streamer.ui.action.execute.Worker;
import org.cosinus.streamer.ui.error.AbortActionException;
import org.cosinus.streamer.ui.error.ActionException;
import org.cosinus.swing.format.FormatHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * {@link Worker} for copying streamers from a source parent streamer to target parent streamer
 */
public class CopyWorker<S extends Streamer<S>, T extends Streamer<T>>
    extends SimpleWorker<CopyProgressModel>
    implements Pipeline<S, Stream<S>, StreamConsumer<S>, CopyStrategy> {

    @Autowired
    protected FormatHandler formatHandler;

    private final ParentStreamer<S> source;

    private final ParentStreamer<T> destination;

    private final StreamerFilter streamerFilter;

    private final CopyStrategy copyStrategy;

    private final OverallCopyListener overallCopyProgress;

    private long totalSize;

    public CopyWorker(CopyActionModel<S, T> copyModel) {
        super(copyModel.getActionId(), new CopyProgressModel());
        this.source = copyModel.getSource();
        this.destination = copyModel.getDestination();
        this.streamerFilter = copyModel.getSourceFilter();
        this.copyStrategy = new CopyStrategy();
        this.overallCopyProgress = new OverallCopyListener();
    }

    @Override
    protected void doWork() {
        try {
            openPipeline();
        } catch (AbortPipelineConsumeException ex) {
            throw new AbortActionException("Copy pipeline aborted", ex);
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, "act_copy_error", source.getPath(), destination.getPath());
        }
    }

    @Override
    public CopyStrategy getPipelineStrategy() {
        return copyStrategy;
    }

    @Override
    public PipelineListener<S> getPipelineListener() {
        return overallCopyProgress;
    }

    @Override
    public void preparePipelineOpen(CopyStrategy pipelineStrategy,
                                    PipelineListener<S> pipelineListener) {
        try (Stream<? extends Streamer<?>> flatStreamers = source.flatStream(streamerFilter)) {
            this.totalSize = flatStreamers
                .filter(not(Streamer::isParent))
                .mapToLong(Streamer::getSize)
                .sum();
        }

        long freeSpace = destination.getFreeSpace();
        if (totalSize > freeSpace && !copyStrategy.shouldContinueWhenNotEnoughFreeSpace()) {
            throw new AbortPipelineConsumeException("Not enough free space on destination: " +
                formatHandler.formatMemorySize(freeSpace));
        }
    }

    @Override
    public Stream<S> openPipelineInputStream(CopyStrategy pipelineStrategy) {
        return source.flatStream(streamerFilter);
    }

    @Override
    public StreamConsumer<S> openPipelineOutputStream(CopyStrategy pipelineStrategy) {
        return this::copyStreamer;
    }

    public void copyStreamer(S streamerToCopy) {
        Path relativePath = getRelativePath(streamerToCopy);
        Path targetPath = destination.getPath().resolve(relativePath);
        T target = destination.create(targetPath, streamerToCopy.isParent());
        BinaryStreamer binarySource = streamerToCopy.binaryStreamer();
        BinaryStreamer binaryTarget = target.binaryStreamer();
        if (binarySource != null && binaryTarget != null) {
            try {
                new CopyBinaryPipeline(binarySource, binaryTarget, copyStrategy, this)
                    .openPipeline();
            } catch (IOException | UncheckedIOException ex) {
                throw new ActionException(ex, "act_copy_error", binarySource.getPath(), binaryTarget.getPath());
            }
        } else if (!target.exists()) {
            target.save();
        }
    }

    private Path getRelativePath(Streamer<?> streamer) {
        Path streamerPath = streamer.getPath();
        return streamerPath.subpath(ofNullable(source.getPath())
                .filter(streamerPath::startsWith)
                .map(Path::getNameCount)
                .orElse(0),
            streamerPath.getNameCount());
    }

    private class OverallCopyListener implements PipelineListener<S> {
        @Override
        public void beforePipelineOpen() {
            updateModel(() -> workerModel.startTotalProgress(totalSize));
        }

        @Override
        public void afterPipelineDataSkip(long skippedDataSize) {
            updateModel(() -> {
                workerModel.updateStreamerProgress(skippedDataSize);
                workerModel.finishStreamerProgress();
            });
        }

        @Override
        public void afterPipelineClose() {
            updateModel(workerModel::finishTotalProgress);
        }
    }
}
