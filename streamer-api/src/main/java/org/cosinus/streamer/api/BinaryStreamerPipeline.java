package org.cosinus.streamer.api;

import org.cosinus.streamer.api.stream.pipeline.binary.BinaryPipeline;
import org.cosinus.streamer.api.stream.pipeline.binary.BinaryPipelineStrategy;

import java.io.InputStream;
import java.io.OutputStream;

public class BinaryStreamerPipeline implements BinaryPipeline {

    private final BinaryStreamer source;

    private final BinaryStreamer target;

    private final BinaryPipelineStrategy transferStrategy;

    public BinaryStreamerPipeline(BinaryStreamer source, BinaryStreamer target, BinaryPipelineStrategy transferStrategy) {
        this.source = source;
        this.target = target;
        this.transferStrategy = transferStrategy;
    }

    @Override
    public BinaryPipelineStrategy getPipelineStrategy() {
        return transferStrategy;
    }

    @Override
    public InputStream inputStream() {
        return source.inputStream();
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return target.outputStream(append);
    }

    @Override
    public long outputSize() {
        return target.getSize();
    }
}
