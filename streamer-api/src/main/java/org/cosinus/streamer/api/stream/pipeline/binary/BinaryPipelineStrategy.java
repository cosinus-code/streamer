package org.cosinus.streamer.api.stream.pipeline.binary;

import org.cosinus.streamer.api.stream.pipeline.PipelineStrategy;

public interface BinaryPipelineStrategy extends PipelineStrategy {

    int DEFAULT_PIPELINE_RATE = 8192;

    default int getPipelineRate() {
        return DEFAULT_PIPELINE_RATE;
    }

    default boolean shouldCheck() {
        return false;
    }

    default boolean shouldAppend() {
        return false;
    }

    default boolean shouldResume() {
        return false;
    }

    default boolean shouldContinueWhenCannotResume(long skippedBytes, long bytesToSkip) {
        return false;
    }

    default boolean shouldContinueWhenCheckFailed() {
        return false;
    }

    default boolean shouldSkipExistingTarget() {
        return false;
    }
}
