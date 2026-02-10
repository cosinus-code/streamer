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

import lombok.Getter;
import org.cosinus.stream.StreamingStrategy;
import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.stream.error.AbortPipelineConsumeException;
import org.cosinus.stream.pipeline.PipelineListener;
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.*;
import org.cosinus.swing.error.ActionException;
import org.cosinus.swing.format.FormatHandler;
import org.cosinus.swing.worker.PipelineWorker;
import org.cosinus.swing.worker.Worker;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.stream.FlatStreamingStrategy.IN_DEPTH;

/**
 * {@link Worker} for copying streamers from a source parent streamer to target parent streamer
 */
public class CopyWorker<S extends Streamer<S>, T extends Streamer<T>>
    extends PipelineWorker<WorkerModel<CopyUnit<S, T>>, CopyUnit<S, T>, CopyProgressModel<S>> {

    @Autowired
    protected FormatHandler formatHandler;

    protected final ParentStreamer<S> source;

    protected final ParentStreamer<T> destination;

    @Getter
    protected final StreamerFilter streamerFilter;

    protected final CopyStrategy copyStrategy;

    private final StreamingStrategy streamingStrategy;

    public CopyWorker(CopyActionModel copyActionModel, WorkerModel<CopyUnit<S, T>> workerModel) {
        super(copyActionModel, workerModel, new CopyProgressModel<>());
        this.source = (ParentStreamer<S>) copyActionModel.getSource();
        this.destination = (ParentStreamer<T>) copyActionModel.getDestination();
        this.streamerFilter = copyActionModel.getSourceFilter();
        this.copyStrategy = new CopyStrategy();
        this.streamingStrategy = new DefaultPipelineStrategy();
    }

    @Override
    public CopyStrategy getPipelineStrategy() {
        return copyStrategy;
    }

    @Override
    public void preparePipelineOpen(final PipelineStrategy pipelineStrategy,
                                    final PipelineListener<CopyUnit<S, T>> pipelineListener) {
        try (Stream<S> flatStreamers = source.flatStream(IN_DEPTH, streamingStrategy, getStreamerFilter())) {
            flatStreamers
                .filter(not(Streamer::isParent))
                .mapToLong(Streamer::getSize)
                .forEach(size -> updateProgress(progressModel -> {
                    progressModel.addTotalProgress(size);
                    progressModel.setTotalItems(progressModel.getTotalItems() + 1);
                }));
        }

        long freeSpace = destination.getFreeSpace();
        if (progressModel.getProgressPercent() > freeSpace && !copyStrategy.shouldContinueWhenNotEnoughFreeSpace()) {
            throw new AbortPipelineConsumeException("Not enough free space on destination: " +
                formatHandler.formatMemorySize(freeSpace));
        }
    }

    @Override
    public Stream<CopyUnit<S, T>> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return source.flatStream(IN_DEPTH, streamingStrategy, getStreamerFilter())
            .map(source -> new CopyUnit<>(source, targetStreamer(source)));
    }

    @Override
    protected StreamConsumer<CopyUnit<S, T>> streamConsumer() {
        return this::copyStreamer;
    }

    protected void copyStreamer(final CopyUnit<S, T> copyUnit) {
        S streamerToCopy = copyUnit.source();
        T streamerToCopyTo = copyUnit.target();
        BinaryStreamer binarySource = streamerToCopy.binaryStreamer();
        BinaryStreamer binaryTarget = streamerToCopyTo.binaryStreamer();
        if (binarySource != null && binaryTarget != null) {
            try {
                new CopyBinaryPipeline(binarySource, binaryTarget, copyStrategy,
                    new CopyBinaryListener(binarySource, binaryTarget))
                    .openPipeline();
            } catch (IOException | UncheckedIOException ex) {
                throw new ActionException(ex, "act_copy_error", binarySource.getPath(), binaryTarget.getPath());
            }
        } else if (!streamerToCopyTo.exists()) {
            streamerToCopyTo.save();
        }
    }

    protected T targetStreamer(final S streamerToCopy) {
        Path targetPath = getTargetPath(streamerToCopy);
        return destination.create(targetPath, streamerToCopy);
    }

    protected Path getTargetPath(final S streamerToCopy) {
        return destination.resolveStreamerPath(source, streamerToCopy);
    }

    protected Path getRelativePath(final Streamer<?> streamer) {
        Path streamerPath = streamer.getPath();
        return streamerPath.subpath(ofNullable(source.getPath())
                .filter(streamerPath::startsWith)
                .map(Path::getNameCount)
                .orElse(0),
            streamerPath.getNameCount());
    }

    @Override
    public void afterPipelineDataSkip(long skippedDataSize) {
        updateProgress(progress -> {
            progress.updateStreamerProgress(skippedDataSize);
            progress.finishStreamerProgress();
        });
    }

    private class CopyBinaryListener implements PipelineListener<byte[]> {

        private final BinaryStreamer source;

        private final BinaryStreamer target;

        public CopyBinaryListener(final BinaryStreamer source,
                                  final BinaryStreamer target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public void beforePipelineOpen() {
            updateProgress(progress -> progress.startStreamerProgress(source, target));
        }

        @Override
        public void afterPipelineDataConsume(final byte[] bytes) {
            updateProgress(progress -> progress.updateStreamerProgress(bytes.length));
        }

        @Override
        public void afterPipelineDataSkip(long skippedDataSize) {
            CopyWorker.this.afterPipelineDataSkip(skippedDataSize);
        }

        @Override
        public void afterPipelineClose(boolean pipelineFailed) {
            source.finalizeStreaming();
            target.finalizeStreaming();
            target.finalizeCopy(source);
            updateProgress(CopyProgressModel::finishStreamerProgress);
        }
    }
}
