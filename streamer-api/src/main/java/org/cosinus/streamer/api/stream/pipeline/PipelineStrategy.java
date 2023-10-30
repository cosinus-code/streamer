package org.cosinus.streamer.api.stream.pipeline;

public interface PipelineStrategy {

    default boolean shouldRetryOnFail() {
        return false;
    }
}
