package org.cosinus.streamer.api.stream.pipeline.error;

public class CancelPipelineConsumeException extends RuntimeException {

    public CancelPipelineConsumeException() {
    }

    public CancelPipelineConsumeException(String message) {
        super(message);
    }

    public CancelPipelineConsumeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelPipelineConsumeException(Throwable cause) {
        super(cause);
    }
}
