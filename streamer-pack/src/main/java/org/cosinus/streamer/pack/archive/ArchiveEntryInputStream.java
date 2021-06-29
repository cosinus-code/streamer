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

package org.cosinus.streamer.pack.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ArchiveEntryInputStream extends FilterInputStream implements EntryInputStream {

    public ArchiveEntryInputStream(ArchiveInputStream archiveInputStream) {
        super(archiveInputStream);
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return ((ArchiveInputStream) in).getNextEntry();
    }

    @Override
    public InputStream getInputStream(ArchiveEntry archiveEntry) {
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public void closeStream() throws IOException {
        super.close();
    }
}
