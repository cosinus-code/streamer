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
package org.cosinus.streamer.pack.archive.save;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.cosinus.streamer.api.stream.consumer.OutputWriter;

import java.io.IOException;

/**
 * Put archive entry
 */
public class PutArchiveEntry implements OutputWriter<ArchiveOutputStream> {

    private final ArchiveEntry archiveEntry;

    public PutArchiveEntry(final ArchiveEntry archiveEntry) {
        this.archiveEntry = archiveEntry;
    }

    @Override
    public void write(ArchiveOutputStream output) throws IOException {
        output.putArchiveEntry(archiveEntry);
    }

    @Override
    public long size() {
        return 0;
    }
}
