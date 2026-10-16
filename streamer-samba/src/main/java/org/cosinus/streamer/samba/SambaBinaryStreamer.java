/*
 * Copyright 2026 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.samba;

import org.cosinus.streamer.api.remote.RemoteBinaryStreamer;
import org.cosinus.streamer.samba.connection.SambaConnection;
import org.cosinus.streamer.samba.connection.SambaFile;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.cosinus.streamer.samba.connection.SambaFile.sambaFile;

public class SambaBinaryStreamer extends SambaStreamer<byte[]>
    implements RemoteBinaryStreamer<SambaFile, SambaConnection> {

    public SambaBinaryStreamer(final Path path, final SambaFile sambaFile) {
        super(path, sambaFile);
    }

    /**
     * The borrowed connection is returned to the pool when the input stream is closed.
     */
    @Override
    public InputStream inputStream() {
        return openStream(connection -> connection.inputStream(getStreamQuery()),
            (inputStream, onClose) -> new FilterInputStream(inputStream) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        onClose.run();
                    }
                }
            });
    }

    /**
     * The borrowed connection is returned to the pool when the output stream is closed.
     */
    @Override
    public OutputStream outputStream(boolean append) {
        return openStream(connection -> connection.outputStream(getRemote(), getStreamQuery(), append),
            (outputStream, onClose) -> new FilterOutputStream(outputStream) {
                @Override
                public void write(byte[] bytes, int offset, int length) throws IOException {
                    out.write(bytes, offset, length);
                }

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        refreshRemote();
                        onClose.run();
                    }
                }
            });
    }

    private <S> S openStream(Function<SambaConnection, S> streamOpener,
                             StreamWrapper<S> streamWrapper) {
        String connectionId = connectionId();
        SambaConnection connection = sambaConnectionPool.borrowConnection(connectionId);
        AtomicBoolean returned = new AtomicBoolean();
        Runnable returnConnection = () -> {
            if (returned.compareAndSet(false, true)) {
                sambaConnectionPool.returnConnection(connectionId, connection);
            }
        };
        try {
            return streamWrapper.wrap(streamOpener.apply(connection), returnConnection);
        } catch (RuntimeException ex) {
            returnConnection.run();
            throw ex;
        }
    }

    @Override
    protected void createAndSave() {
        withConnection(connection -> connection.save(sambaFile(getStreamQuery())));
    }

    @FunctionalInterface
    private interface StreamWrapper<S> {
        S wrap(S stream, Runnable onClose);
    }
}
