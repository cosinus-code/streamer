package org.cosinus.streamer.api.stream.pipeline.binary;

import org.cosinus.streamer.api.stream.binary.BinaryStream;
import org.cosinus.streamer.api.stream.pipeline.Pipeline;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.stream.pipeline.error.AbortPipelineConsumeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static java.lang.String.format;

public interface BinaryPipeline extends Pipeline<byte[], BinaryStream, BinaryStreamConsumer, BinaryPipelineStrategy> {

    @Override
    default BinaryStream openPipelineInputStream(final BinaryPipelineStrategy pipelineStrategy) {
        return BinaryStream.of(inputStream(), pipelineStrategy.getPipelineRate());
    }

    @Override
    default BinaryStreamConsumer openPipelineOutputStream(final BinaryPipelineStrategy pipelineStrategy) {
        boolean append = pipelineStrategy.shouldAppend() || pipelineStrategy.shouldResume();
        return new BinaryStreamConsumer(outputStream(append));
    }

    @Override
    default void preparePipelineConsume(final BinaryStream pipelineInputStream,
                                        final BinaryStreamConsumer pipelineOutputStream,
                                        final BinaryPipelineStrategy pipelineStrategy,
                                        PipelineListener<byte[]> pipelineListener) throws IOException {
        if (pipelineStrategy.shouldResume()) {
            long bytesToSkip = outputSize();
            if (bytesToSkip > 0) {
                long skippedBytes = pipelineInputStream.skipBytes(bytesToSkip);
                if (skippedBytes != bytesToSkip &&
                    !pipelineStrategy.shouldContinueWhenCannotResume(skippedBytes, bytesToSkip)) {
                    throw new AbortPipelineConsumeException(
                        format("Pipeline aborted by user after resume not match: expected to skip %d but was %d",
                            bytesToSkip, skippedBytes));
                }
                pipelineListener.afterPipelineDataSkip(skippedBytes);
//                throw new SkipPipelineConsumeException(skippedBytes,
//                    format("Skipped %d bytes for resume", skippedBytes));
            }
        }
    }

    @Override
    default void checkPipelineConsume(BinaryStream pipelineInputStream,
                                      BinaryStreamConsumer pipelineOutputStream,
                                      BinaryPipelineStrategy pipelineStrategy,
                                      PipelineListener<byte[]> listener) {
        if (pipelineStrategy.shouldCheck()) {
            Optional<String> inputChecksum = pipelineInputStream.checksum();
            Optional<String> outputChecksum = pipelineOutputStream.checksum();
            if (inputChecksum.equals(outputChecksum) &&
                !pipelineStrategy.shouldContinueWhenCheckFailed()) {
                throw new AbortPipelineConsumeException(
                    format("Pipeline aborted by user after consumed stream verification failed: " +
                            "expected %s checksum but was %s",
                        inputChecksum, outputChecksum));
            }
        }
    }

    InputStream inputStream();

    OutputStream outputStream(boolean append);

    long outputSize();
}
