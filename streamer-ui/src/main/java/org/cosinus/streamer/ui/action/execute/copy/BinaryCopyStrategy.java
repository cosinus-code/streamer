package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.stream.pipeline.binary.BinaryPipelineStrategy;

public class BinaryCopyStrategy implements BinaryPipelineStrategy {

    private final CopyWorker copyWorker;

    private final BinaryStreamer source;

    private final BinaryStreamer target;

    public BinaryCopyStrategy(BinaryStreamer source, BinaryStreamer target, CopyWorker copyWorker) {
        this.source = source;
        this.target = target;
        this.copyWorker = copyWorker;
    }

    @Override
    public boolean shouldCheck() {
        return copyWorker.getCopyModel().shouldCheckTransfer();
    }

    @Override
    public boolean shouldAppend() {
        return copyWorker.getCopyModel().shouldAppendSourceToCurrentTarget();
    }

    @Override
    public boolean shouldResume() {
        return copyWorker.getCopyModel().shouldResume();
    }

    @Override
    public boolean shouldRetryOnFailed(byte[] bytes) {
        return copyWorker.shouldRetryOnFailed(target);
    }

    @Override
    public boolean shouldContinueWhenCannotResume(long skippedBytes, long bytesToSkip) {
        return copyWorker.shouldContinueWhenCannotResume(skippedBytes, source, target);
    }

    @Override
    public boolean shouldContinueWhenCheckFailed() {
        return copyWorker.shouldContinueWhenCopyCheckFailed(source, target);
    }

    @Override
    public void finalizeChunkData(byte[] data) {
        copyWorker.finishBytesCopy(data);
    }

    @Override
    public void finalizeData() {
        copyWorker.finishStreamerCopy();
    }
}
