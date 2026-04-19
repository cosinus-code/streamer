/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.api.worker;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.boot.cleanup.ApplicationShutDown;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.Worker;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.lang.Math.min;
import static org.cosinus.swing.format.FormatHandler.KILO_INT;

public abstract class ChannelWorker extends Worker<WorkerModel<byte[]>, byte[], ProgressModel> {

    protected static final long DEFAULT_BUFFER_SIZE = KILO_INT;

    @Autowired
    private ApplicationShutDown applicationShutDown;

    private final BinaryStreamer binaryStreamer;

    protected ChannelWorker(final ActionModel actionModel,
                            final WorkerModel<byte[]> workerModel,
                            final ProgressModel progressModel,
                            final BinaryStreamer binaryStreamer) {
        super(actionModel, workerModel, progressModel);
        this.binaryStreamer = binaryStreamer;
    }

    @Override
    protected void doWork() {
        try {
            FileChannel fileChannel = binaryStreamer.fileChannel();
            if (!isCancelled() && fileChannel.isOpen()) {
                applicationShutDown.register(binaryStreamer.getId(), fileChannel);
                long offset = offset();
                long limit = limit();
                int length = (int) min(limit, fileChannel.size() - offset);
                ByteBuffer buffer = ByteBuffer.allocateDirect(length);
                int count = fileChannel.read(buffer, offset());
                buffer.flip();
                if (count >= 0) {
                    byte[] result = new byte[buffer.remaining()];
                    buffer.get(result);
                    checkStatusAndPublish(result);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected abstract long offset();

    protected long limit() {
        return DEFAULT_BUFFER_SIZE;
    }
}
