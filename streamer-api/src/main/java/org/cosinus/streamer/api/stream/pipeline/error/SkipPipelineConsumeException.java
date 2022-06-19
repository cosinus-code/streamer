package org.cosinus.streamer.api.stream.pipeline.error;

public class SkipPipelineConsumeException extends RuntimeException {

    private long skippedSize;

    public SkipPipelineConsumeException(long skippedSize) {
        this.skippedSize = skippedSize;
    }

    public SkipPipelineConsumeException(long skippedSize, String message) {
        super(message);
        this.skippedSize = skippedSize;
    }

    public long getSkippedSize() {
        return skippedSize;
    }
}
