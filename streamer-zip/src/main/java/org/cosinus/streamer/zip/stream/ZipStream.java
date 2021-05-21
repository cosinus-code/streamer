/*
 * Copyright 2020 Cosinus Software
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

package org.cosinus.streamer.zip.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@link Stream} for {@link ZipStreamEntry}
 */
public class ZipStream {

    public static Stream<ZipStreamEntry> zipStream(InputStream inputStream) {
        return stream(new ZipEntryInputStream(inputStream));
    }

    public static Stream<ZipStreamEntry> walk(InputStream inputStream,
                                              String topPath) {
        return zipStream(inputStream)
                .filter(zipEntry -> zipEntry.getName().startsWith(topPath));
    }

    protected static Stream<ZipStreamEntry> stream(ZipEntryInputStream zipEntryInputStream) {
        return StreamSupport
                .stream(new ZipSpliterator(zipEntryInputStream), false)
                .onClose(() -> close(zipEntryInputStream));
    }

    protected static void close(ZipEntryInputStream zipEntryInputStream) {
        try {
            zipEntryInputStream.closeZipStream();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
