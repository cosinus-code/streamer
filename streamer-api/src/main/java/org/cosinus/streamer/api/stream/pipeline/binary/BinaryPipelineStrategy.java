package org.cosinus.streamer.api.stream.pipeline.binary;

import org.cosinus.streamer.api.stream.binary.BinaryStream;
import org.cosinus.streamer.api.stream.pipeline.PipelineStrategy;

import java.io.IOException;
import java.util.function.Supplier;

public interface BinaryPipelineStrategy extends PipelineStrategy<byte[]> {

    int DEFAULT_PIPELINE_RATE = 8192;

    default int getPipelineRate() {
        return DEFAULT_PIPELINE_RATE;
    }

    default boolean prepareResume(BinaryStream pipelineInputStream, Supplier<Long> outputSizeSupplier) throws IOException {
        if (shouldResume()) {
            long bytesToSkip = outputSizeSupplier.get();
            long skippedBytes = pipelineInputStream.skipBytes(bytesToSkip);
            if (skippedBytes != bytesToSkip) {
                return shouldContinueWhenCannotResume(skippedBytes, bytesToSkip);
            }
        }

        return true;
    }
}
