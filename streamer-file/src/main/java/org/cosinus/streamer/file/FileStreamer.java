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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.value.*;
import org.cosinus.swing.util.AutoRemovableTemporaryFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.streamer.file.FileMainStreamer.FILE_PROTOCOL;

public abstract class FileStreamer<T> implements Streamer<T> {

    protected static final String DETAIL_KEY_NAME = "name";
    protected static final String DETAIL_KEY_TYPE = "type";
    protected static final String DETAIL_KEY_SIZE = "size";
    protected static final String DETAIL_KEY_TIME = "time";

    protected final FileMainStreamer fileMainStreamer;

    protected final FileHandler fileHandler;

    protected final File file;

    protected List<TranslatableName> detailNames;

    protected Map<TranslatableName, Value> details;

    public FileStreamer(FileMainStreamer fileMainStreamer, FileHandler fileHandler, Path path) {
        this.fileMainStreamer = fileMainStreamer;
        this.fileHandler = fileHandler;
        this.file = path.toFile();
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
                .orElseGet(() -> new FileParentStreamer(fileMainStreamer, fileHandler, parentPath)))
            .orElse(fileMainStreamer);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        return new FileBinaryStreamer(fileMainStreamer, fileHandler, path);
    }

    @Override
    public void save() {
        if (exists()) {
            ofNullable(details.get(new TranslatableName(DETAIL_KEY_NAME, null)))
                .map(Value::toString)
                .filter(not(getName()::equals))
                .ifPresent(this::rename);
        } else {
            createAndSave();
        }
    }

    protected abstract  void createAndSave();

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
        if (detailNames == null) {
            initDetails();
        }
        return detailNames;
    }

    @Override
    public Map<TranslatableName, Value> details() {
        if (detailNames == null) {
            details();
        }
        return details;
    }

    public void initDetails() {
        this.details = Stream.of(
                ImmutablePair.of(DETAIL_KEY_NAME, new TextValue(getName())),
                ImmutablePair.of(DETAIL_KEY_TYPE, new TextValue(getType())),
                ImmutablePair.of(DETAIL_KEY_SIZE, new MemoryValue(getSize())),
                ImmutablePair.of(DETAIL_KEY_TIME, new DateValue(lastModified())))
            .collect(Collectors.toMap(pair ->
                new TranslatableName(pair.getKey(), null),
                Pair::getValue,
                (key1, key2) -> key1,
                LinkedHashMap::new));
        detailNames = new ArrayList<>(details.keySet());
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
