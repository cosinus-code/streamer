package org.cosinus.streamer.api.stream.pipeline.binary;

import org.cosinus.streamer.api.error.ConsumedStreamNotMatchException;
import org.cosinus.streamer.api.stream.binary.BinaryStream;
import org.cosinus.streamer.api.stream.pipeline.Pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    default boolean prepare(final BinaryStream pipelineInputStream,
                            final BinaryStreamConsumer pipelineOutputStream,
                            final BinaryPipelineStrategy pipelineStrategy) throws IOException {
        return pipelineStrategy.prepareResume(pipelineInputStream, this::outputSize);
    }

    @Override
    default void check(BinaryStream pipelineInputStream,
                       BinaryStreamConsumer pipelineOutputStream,
                       BinaryPipelineStrategy pipelineStrategy) {
        if (pipelineStrategy.shouldCheck() &&
            pipelineInputStream.checksum() != pipelineOutputStream.checksum() &&
            !pipelineStrategy.shouldContinueWhenCheckFailed()) {
            throw new ConsumedStreamNotMatchException("Consumed stream verification failed");
        }
    }

    @Override
    default void finalizeData(final BinaryPipelineStrategy pipelineStrategy) {

    }

    InputStream inputStream();

    OutputStream outputStream(boolean append);

    long outputSize();
}
