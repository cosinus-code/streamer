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

package org.cosinus.streamer.file;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.value.*;
import org.cosinus.swing.util.AutoRemovableTemporaryFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.streamer.file.FileMainStreamer.FILE_PROTOCOL;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class FileStreamer<T> implements Streamer<T> {

    protected static final String DETAIL_KEY_NAME = "name";
    protected static final String DETAIL_KEY_TYPE = "type";
    protected static final String DETAIL_KEY_SIZE = "size";
    protected static final String DETAIL_KEY_TIME = "time";
    protected static final String DETAIL_KEY_FREE_MEMORY = "detail_free_memory";

    @Autowired
    protected FileMainStreamer fileMainStreamer;

    @Autowired
    protected FileHandler fileHandler;

    protected final File file;

    protected final List<TranslatableName> detailNames;

    protected List<Value> details;

    public FileStreamer(Path path) {
        injectContext(this);
        this.file = path.toFile();
        detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_TYPE, null),
            new TranslatableName(DETAIL_KEY_SIZE, null),
            new TranslatableName(DETAIL_KEY_TIME, null));
    }

    public File getFile() {
        return file;
    }

    @Override
    public Path getPath() {
        return file.toPath();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return ofNullable(file.toPath().getParent())
            .map(parentPath -> fileMainStreamer
                .stream()
                .filter(fileStreamer -> fileStreamer.getPath().equals(parentPath))
                .findFirst()
                .map(ParentStreamer.class::cast)
                .orElseGet(() -> new FileParentStreamer(parentPath)))
            .orElse(fileMainStreamer);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public void save() {
        if (exists()) {
            ofNullable(details.get(0))
                .map(Value::toString)
                .filter(not(getName()::equals))
                .ifPresent(this::rename);
        } else {
            createAndSave();
        }
    }

    protected abstract void createAndSave();

    protected boolean rename(String newName) {
        File fileToRename = file;
        Path newPath = Paths.get(fileToRename.getParent(), newName);
        File newFile = newPath.toFile();
        boolean samePath = fileToRename.toPath().equals(newPath);
        if (newFile.exists() && !samePath) {
            throw new StreamerException("rename.error.already.exist", newPath);
        }

        if (!samePath) {
            return fileToRename.renameTo(newFile);
        }

        try (AutoRemovableTemporaryFile tmpFile = new AutoRemovableTemporaryFile(fileToRename)) {
            return fileToRename.renameTo(tmpFile.getFile()) &&
                tmpFile.getFile().renameTo(newFile);
        } catch (IOException ex) {
            throw new StreamerException("rename.error.cannot.create.temporary.file", fileToRename, ex);
        }
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean canUpdate() {
        return file.canWrite();
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public boolean delete() {
        return file.delete() && !file.exists();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public String getProtocol() {
        return FILE_PROTOCOL;
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public List<Value> details() {
        init();
        return details;
    }

    public void init() {
        if (details == null) {
            details = asList(
                new TextValue(getName()),
                new TextValue(getType()),
                new MemoryValue(getSize()),
                new DateValue(lastModified()));
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FileStreamer that)) {
            return false;
        }

        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
