package org.cosinus.streamer.api.stream.pipeline;

public interface PipelineStrategy<D> {

    boolean shouldCheck();

    boolean shouldAppend();

    boolean shouldResume();

    boolean shouldRetryOnFailed(D data);

    boolean shouldContinueWhenCannotResume(long skippedBytes, long bytesToSkip);

    boolean shouldContinueWhenCheckFailed();

    void finalizeChunkData(D data);

    void finalizeData();
}
