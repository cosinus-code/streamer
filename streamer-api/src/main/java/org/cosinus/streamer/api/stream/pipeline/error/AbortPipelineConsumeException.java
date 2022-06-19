package org.cosinus.streamer.api.stream.pipeline.error;

public class AbortPipelineConsumeException extends RuntimeException {

    public AbortPipelineConsumeException(String message) {
        super(message);
    }
}
