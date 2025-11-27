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

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.stream.StreamingStrategy;
import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.stream.error.AbortPipelineConsumeException;
import org.cosinus.stream.pipeline.Pipeline;
import org.cosinus.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.*;
import org.cosinus.swing.error.AbortActionException;
import org.cosinus.swing.error.ActionException;
import org.cosinus.swing.worker.SimpleWorker;
import org.cosinus.swing.worker.Worker;
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

    protected final ParentStreamer<S> source;

    protected final ParentStreamer<T> destination;

    protected final StreamerFilter streamerFilter;

    protected final CopyStrategy copyStrategy;

    protected final OverallCopyListener overallCopyProgress;

    private final StreamingStrategy streamingStrategy;

    protected long totalSize;

    protected long totalItems;

    public CopyWorker(CopyActionModel<S, T> copyModel) {
        super(copyModel, new CopyProgressModel());
        this.source = copyModel.getSource();
        this.destination = copyModel.getDestination();
        this.streamerFilter = copyModel.getSourceFilter();
        this.copyStrategy = new CopyStrategy();
        this.overallCopyProgress = createOverallCopyListener();
        this.streamingStrategy = new DefaultStreamingStrategy();
    }

    public StreamerFilter getStreamerFilter() {
        return streamerFilter;
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
        try (Stream<S> flatStreamers = source.flatStream(streamingStrategy, streamerFilter)) {
            flatStreamers
                .filter(not(Streamer::isParent))
                .mapToLong(Streamer::getSize)
                .forEach(size -> {
                    this.totalSize += size;
                    pipelineListener.onPreparingPipeline(++this.totalItems);
                });
        }

        long freeSpace = destination.getFreeSpace();
        if (totalSize > freeSpace && !copyStrategy.shouldContinueWhenNotEnoughFreeSpace()) {
            throw new AbortPipelineConsumeException("Not enough free space on destination: " +
                formatHandler.formatMemorySize(freeSpace));
        }
    }

    @Override
    public Stream<S> openPipelineInputStream(CopyStrategy pipelineStrategy) {
        return source.flatStream(streamingStrategy, getStreamerFilter());
    }

    @Override
    public StreamConsumer<S> openPipelineOutputStream(CopyStrategy pipelineStrategy) {
        return this::copyStreamer;
    }

    protected void copyStreamer(S streamerToCopy) {
        copyStreamer(streamerToCopy, targetStreamer(streamerToCopy));
    }

    protected void copyStreamer(S streamerToCopy, T streamerToCopyTo) {
        BinaryStreamer binarySource = streamerToCopy.binaryStreamer();
        BinaryStreamer binaryTarget = streamerToCopyTo.binaryStreamer();
        if (binarySource != null && binaryTarget != null) {
            try {
                new CopyBinaryPipeline(binarySource, binaryTarget, copyStrategy, this)
                    .openPipeline();
            } catch (IOException | UncheckedIOException ex) {
                throw new ActionException(ex, "act_copy_error", binarySource.getPath(), binaryTarget.getPath());
            }
        } else if (!streamerToCopyTo.exists()) {
            streamerToCopyTo.save();
        }
    }

    protected T targetStreamer(S streamerToCopy) {
        Path targetPath = getTargetPath(streamerToCopy);
        return destination.create(targetPath, streamerToCopy);
    }

    protected Path getTargetPath(S streamerToCopy) {
        return destination.getPath().resolve(getRelativePath(streamerToCopy));
    }

    protected Path getRelativePath(Streamer<?> streamer) {
        Path streamerPath = streamer.getPath();
        return streamerPath.subpath(ofNullable(source.getPath())
                .filter(streamerPath::startsWith)
                .map(Path::getNameCount)
                .orElse(0),
            streamerPath.getNameCount());
    }

    protected OverallCopyListener createOverallCopyListener() {
        return new OverallCopyListener();
    }

    protected class OverallCopyListener implements PipelineListener<S> {

        @Override
        public void onPreparingPipeline(long totalItems) {
            updateModel(() -> workerModel.setTotalItems(totalItems));
        }

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
        public void afterPipelineClose(boolean pipelineFailed) {
            updateModel(workerModel::finishTotalProgress);
        }
    }
}
