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
import org.cosinus.streamer.ui.action.execute.ProgressWorker;
import org.cosinus.streamer.ui.error.AbortActionException;
import org.cosinus.streamer.ui.error.ActionException;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * {@link ProgressWorker} for copying streamers from a source parent streamer to target parent streamer
 */
public class CopyWorker<S extends Streamer<?>, T extends Streamer<?>>
    extends ProgressWorker<CopyProgressModel>
    implements Pipeline<S, Stream<S>, StreamConsumer<S>, CopyStrategy> {

    private final ParentStreamer<S> source;

    private final ParentStreamer<T> destination;

    private final StreamerFilter streamerFilter;

    private final CopyStrategy copyStrategy;

    private final OverallCopyListener overallCopyProgress;

    private long totalSize;

    public CopyWorker(CopyActionModel<S, T> copyModel,
                      Window parentWindow) {
        super(parentWindow, copyModel.getActionId(), new CopyProgressModel(copyModel.getActionId()));
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
        this.totalSize = source.getTotalSize(streamerFilter);
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
        return streamerToCopy -> {
            checkWorkerStatus();
            ofNullable(streamerToCopy.binaryStreamer())
                .ifPresentOrElse(
                    this::copyBinaryStreamer,
                    () -> createTargetStreamer(streamerToCopy));
        };
    }

    private void createTargetStreamer(Streamer<?> sourceStreamer) {
        Path targetPath = buildTargetPath(sourceStreamer);
        T target = destination.create(targetPath, sourceStreamer.isParent());
        if (!target.exists()) {
            target.save();
        }
    }

    private void copyBinaryStreamer(BinaryStreamer binarySource) {
        Path targetPath = buildTargetPath(binarySource);
        BinaryStreamer binaryTarget = binarySource.createBinaryStreamer(targetPath);
        CopyBinaryPipeline copyBinaryPipeline =
            new CopyBinaryPipeline(binarySource, binaryTarget, copyStrategy, this);

        try {
            copyBinaryPipeline.openPipeline();
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, "act_copy_error", binarySource.getPath(), binaryTarget.getPath());
        }
    }

    private Path buildTargetPath(Streamer<?> streamerToCopy) {
        Path relativePath = getRelativePath(streamerToCopy);
        return destination.getPath().resolve(relativePath);
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
            publishProgress(() -> progress.startTotalProgress(totalSize));
        }

        @Override
        public void afterPipelineDataSkip(long skippedDataSize) {
            publishProgress(() -> {
                progress.updateStreamerProgress(skippedDataSize);
                progress.finishStreamerProgress();
            });
        }

        @Override
        public void afterPipelineClose() {
            publishProgress(progress::finishTotalProgress);
        }
    }
}
