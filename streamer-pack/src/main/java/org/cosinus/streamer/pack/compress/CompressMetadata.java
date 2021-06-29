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

package org.cosinus.streamer.pack.compress;

public class CompressMetadata {

    private String name;

    private long size;

    private long modificationTime;

    public String getName() {
        return name;
    }

    public CompressMetadata setName(String name) {
        this.name = name;
        return this;
    }

    public long getSize() {
        return size;
    }

    public CompressMetadata setSize(long size) {
        this.size = size;
        return this;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public CompressMetadata setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
        return this;
    }
}
