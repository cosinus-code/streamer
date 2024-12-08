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
package org.cosinus.streamer.api;

import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;
import java.util.List;

public abstract class StreamerDelegate<T, S extends Streamer<?>> implements Streamer<T> {
    protected final S delegate;

    public StreamerDelegate(S delegate) {
        this.delegate = delegate;
    }

    @Override
    public BinaryStreamer binaryStreamer() {
        return delegate.binaryStreamer();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return delegate.getParent();
    }

    @Override
    public void save() {
        delegate.save();
    }

    @Override
    public boolean delete() {
        return delegate.delete();
    }

    @Override
    public String getProtocol() {
        return delegate.getProtocol();
    }

    @Override
    public Path getPath() {
        return delegate.getPath();
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public long getSize() {
        return delegate.getSize();
    }

    @Override
    public long lastModified() {
        return delegate.lastModified();
    }

    @Override
    public boolean canRead() {
        return delegate.canRead();
    }

    @Override
    public boolean canUpdate() {
        return delegate.canUpdate();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public boolean isLink() {
        return delegate.isLink();
    }

    @Override
    public boolean isHidden() {
        return delegate.isHidden();
    }

    @Override
    public String getIconName() {
        return delegate.getIconName();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getUrlPath() {
        return delegate.getUrlPath();
    }

    @Override
    public boolean isParent() {
        return delegate.isParent();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public boolean isTextCompatible() {
        return delegate.isTextCompatible();
    }

    @Override
    public boolean isImage() {
        return delegate.isImage();
    }

    @Override
    public boolean isDirty() {
        return delegate.isDirty();
    }

    @Override
    public boolean isFile() {
        return delegate.isFile();
    }

    @Override
    public List<TranslatableName> detailNames() {
        return delegate.detailNames();
    }

    @Override
    public List<Value> details() {
        return delegate.details();
    }

    @Override
    public int getLeadDetailIndex() {
        return delegate.getLeadDetailIndex();
    }
}
