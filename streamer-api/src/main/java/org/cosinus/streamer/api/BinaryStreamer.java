/*
 * Copyright 2025 Cosinus Software
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

package org.cosinus.streamer.api;

import org.cosinus.streamer.api.stream.binary.BinaryStream;

import java.io.InputStream;
import java.io.OutputStream;

import static org.cosinus.swing.format.FormatHandler.MEGA_INT;

public interface BinaryStreamer extends Streamer<byte[]> {

    int DEFAULT_TRANSFER_RATE = MEGA_INT;

    @Override
    default BinaryStream stream() {
        return BinaryStream.of(inputStream(), getTransferRate());
    }

    @Override
    default BinaryStreamer binaryStreamer()
    {
        return this;
    }

    InputStream inputStream();

    OutputStream outputStream(boolean append);

    default int getTransferRate() {
        return DEFAULT_TRANSFER_RATE;
    }

    default void finalizeStreaming() {
    }
}
