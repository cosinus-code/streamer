package org.cosinus.streamer.api.stream.pipeline;

public interface PipelineListener<D> {

    default void beforePipelineOpen() {
    }

    default void afterPipelineOpen() {
    }

    default void beforePipelineDataConsume(D data) {
    }

    default void afterPipelineDataConsume(D data) {
    }

    default void afterPipelineDataSkip(long skippedDataSize) {
    }

    default void beforePipelineClose() {
    }

    default void afterPipelineClose(boolean pipelineFailed) {
    }

    default void onPipelineFail() {
    }
}
