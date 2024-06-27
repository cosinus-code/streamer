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
package org.cosinus.streamer.pack.archive.stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.cosinus.streamer.pack.archive.ArchiveStreamEntry;

import java.util.Collection;

/**
 * Interface for an archive cache
 */
public interface ArchiveCache {

    void add(ArchiveStreamEntry archiveEntry);

    boolean contains(ArchiveEntry archiveEntry);

    boolean isLoaded();

    void setLoaded(boolean loaded);

    void evict();

    boolean evict(ArchiveStreamEntry archiveEntry);

    Collection<ArchiveStreamEntry> additionalEntries();
}
