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

import static java.lang.Math.clamp;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.cosinus.swing.format.FormatHandler.KILO_INT;

/**
 * Worker which reads the bytes of a {@link BinaryStreamer} through its {@link FileChannel},
 * from an offset and up to a limit, in chunks passed to {@link #consume(byte[])}.
 *
 * @param <M> the type of the worker model
 * @param <T> the type of the items published by the worker
 */
public abstract class ChannelWorker<M extends WorkerModel<T>, T> extends Worker<M, T, ProgressModel> {

    protected static final long DEFAULT_BUFFER_SIZE = KILO_INT;

    @Autowired
    private ApplicationShutDown applicationShutDown;

    protected final BinaryStreamer binaryStreamer;

    protected ChannelWorker(final ActionModel actionModel,
                            final M workerModel,
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
                readChannel(fileChannel);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Read the bytes from the offset up to the limit (or the end of the channel),
     * in chunks of at most {@link #chunkSize()} bytes.
     * A chunk is filled with as many reads as needed, as a read can return fewer bytes than asked.
     */
    private void readChannel(FileChannel fileChannel) throws IOException {
        long position = offset();
        long end = position + clamp(fileChannel.size() - position, 0, limit());
        ByteBuffer buffer = ByteBuffer.allocate((int) min(max(1, chunkSize()), max(0, end - position)));
        while (position < end && !isCancelled()) {
            buffer.clear();
            buffer.limit((int) min(buffer.capacity(), end - position));
            while (buffer.hasRemaining()) {
                if (fileChannel.read(buffer, position + buffer.position()) <= 0) {
                    break;
                }
            }
            buffer.flip();
            if (!buffer.hasRemaining()) {
                break;
            }
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            position += bytes.length;
            consume(bytes);
        }
    }

    /**
     * Consume a chunk of bytes read from the channel.
     *
     * @param bytes the bytes read
     */
    protected abstract void consume(byte[] bytes);

    protected abstract long offset();

    protected long limit() {
        return DEFAULT_BUFFER_SIZE;
    }

    /**
     * @return the maximum size of a chunk passed to {@link #consume(byte[])}; by default, all bytes up to the limit
     */
    protected int chunkSize() {
        return (int) min(Integer.MAX_VALUE, limit());
    }
}
